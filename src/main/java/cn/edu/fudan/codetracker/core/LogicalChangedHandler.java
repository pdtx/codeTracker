package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.diff.CldiffAdapter;
import cn.edu.fudan.codetracker.domain.diff.DiffInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * description: 逻辑上的改变映射
 *
 * @author fancying
 * create: 2020-03-20 19:27
 **/
@Slf4j
public class LogicalChangedHandler implements NodeMapping {

    private CommonInfo commonInfo;
    private ProxyDao proxyDao;

    private static ThreadLocal< Map<String, List<DiffInfo>> > mapThreadLocal = new ThreadLocal<>();

    private LogicalChangedHandler(){}

    static LogicalChangedHandler getInstance(){
        return MappingGeneratorHolder.LOGICAL_CHANGED_HANDLER;
    }

    private static final class MappingGeneratorHolder {
        private static final LogicalChangedHandler LOGICAL_CHANGED_HANDLER = new LogicalChangedHandler();
    }

    private AddHandler addHandler = AddHandler.getInstance();
    private DeleteHandler deleteHandler = DeleteHandler.getInstance();
    private PhysicalChangedHandler physicalChangedHandler = PhysicalChangedHandler.getInstance();


    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo, ProxyDao proxyDao) {
        this.commonInfo = commonInfo;
        this.proxyDao = proxyDao;
        Map<String, List<DiffInfo>> diffMap = mapThreadLocal.get();

        if (preRoot instanceof FileNode && curRoot instanceof FileNode) {
            curRoot.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
            NodeMapping.setNodeMapped(preRoot, curRoot, proxyDao, commonInfo);

            //抽取preMap和curMap
            // key: [class method field statement] value:[BaseNode]
            Map<ProjectInfoLevel, Set<BaseNode>> preMap = extractMapFromNode(preRoot);
            Map<ProjectInfoLevel, Set<BaseNode>> curMap = extractMapFromNode(curRoot);

            ProjectInfoLevel[] keys = {ProjectInfoLevel.CLASS, ProjectInfoLevel.METHOD, ProjectInfoLevel.FIELD};

            //先mapping class、method、field
            for (ProjectInfoLevel p : keys) {
                Set<BaseNode> preSet = preMap.get(p);
                Set<BaseNode> curSet = curMap.get(p);
                Set<DiffInfo> diffSet = new HashSet<>(diffMap.get(p.getName()));
                mapping(preSet, curSet, diffSet, proxyDao);
            }

            //再mapping属于同一method的statement
            Set<DiffInfo> statementDiffs = new HashSet<>(diffMap.get("statement"));
            for (BaseNode node: curMap.get(ProjectInfoLevel.METHOD)) {
                MethodNode methodNode = (MethodNode)node;
                Set<BaseNode> statements = getStatementNodeFromMethod(methodNode);
                Set<BaseNode> preStatements = null;
                if(methodNode.getPreMappingBaseNode() != null) {
                    MethodNode preMethodNode = (MethodNode)methodNode.getPreMappingBaseNode();
                    preStatements = getStatementNodeFromMethod(preMethodNode);
                }
                mapping(preStatements, statements, statementDiffs, proxyDao);
            }
        }
    }

    private Set<BaseNode> getStatementNodeFromMethod(MethodNode methodNode) {
        Set<BaseNode> set = new HashSet<>();
        Queue<BaseNode> queue = new ArrayDeque<>();
        queue.offer(methodNode);
        while (queue.size() != 0) {
            BaseNode baseNode = queue.poll();
            if (baseNode instanceof StatementNode) {
                StatementNode statementNode = (StatementNode)baseNode;
                statementNode.setMethodUuid(methodNode.getRootUuid());
                set.add(baseNode);
            }
            if (baseNode.getChildren() != null) {
                for (BaseNode child: baseNode.getChildren()) {
                    queue.offer(child);
                }
            }
        }
        return set;
    }

    private void mapping(Set<BaseNode> preSet, Set<BaseNode> curSet, Set<DiffInfo> diffSet, ProxyDao proxyDao) {
        // 遍历diffInfo 处理有diff信息的节点
        for (DiffInfo diffInfo : diffSet) {
            BaseNode pre = diffInfo.findChangeNode(preSet,false);
            BaseNode cur = diffInfo.findChangeNode(curSet,true);
            if (pre == null && cur == null) {
                log.warn("useless diff info:[{}]", diffInfo.toString());
                continue;
            }
            if (pre == null) {
                dealWithAddDelete(cur, diffInfo, addHandler, curSet, proxyDao);
                continue;
            }
            if (cur == null) {
                dealWithAddDelete(pre, diffInfo, deleteHandler, preSet, proxyDao);
                continue;
            }

            if (cur instanceof StatementNode) {
                ((StatementNode) cur).setDescription(diffInfo.getDescription());
            }
            if (cur instanceof MethodNode) {
                ((MethodNode) cur).setDiff(diffInfo.getJsonObject());
            }
            BaseNode.ChangeStatus status = cur.getChangeStatus();
            if ("Change".equals(diffInfo.getChangeRelation())) {
                status = BaseNode.ChangeStatus.SELF_CHANGE;
            } else if ("Move".equals(diffInfo.getChangeRelation())){
                status = BaseNode.ChangeStatus.MOVE;
            }
            cur.setChangeStatus(status);
            BaseNode tmp = cur;
            backTracing(tmp);

            NodeMapping.setNodeMapped(pre, cur, proxyDao, commonInfo);
            preSet.remove(pre);
            curSet.remove(cur);
        }

        // 处理没有diff信息的节点
        if (curSet == null && preSet == null) {
            return;
        }
        dealWithoutDiff(preSet, curSet);
    }


    /**
     * todo
     */
    private StatementNode findMostSimilarStatement(StatementNode cStatement, Set<BaseNode> preSet) {
        return null;
    }

    private void dealWithoutDiff(Set<BaseNode> preSet, Set<BaseNode> curSet) {
        if (preSet == null) {
            curSet.forEach(node -> addHandler.subTreeMapping(null, node, commonInfo, proxyDao));
            return;
        }

        if (curSet == null) {
            preSet.forEach(node -> deleteHandler.subTreeMapping(node, null, commonInfo, proxyDao));
            return;
        }

        //再遍历剩下的（不变或者物理改变）
        for (BaseNode node: curSet) {
            if (node instanceof StatementNode) {
                // statement
                StatementNode cStatement = (StatementNode) node;
                StatementNode pStatement = findMostSimilarStatement(cStatement, preSet);

                // todo 待完善
                if (pStatement == null) {
                    log.warn("pre Statement is null");
                    continue;
                }

                boolean isSameLine = cStatement.getBegin() == pStatement.getBegin() && cStatement.getEnd() == pStatement.getEnd();
                boolean isSameContent =  cStatement.getBody().equals(pStatement.getBody());
                if (!isSameContent || !isSameLine) {
                    physicalChangedHandler.subTreeMapping(pStatement, cStatement, commonInfo, proxyDao);
                }
                preSet.remove(pStatement);
                continue;
            }

            if (node instanceof MethodNode) {
                // 在preSet里面找到 与curNode 匹配的 node
                for (BaseNode preNode: preSet) {
                    MethodNode pMethod = (MethodNode)preNode;
                    MethodNode cMethod = (MethodNode)node;
                    if (pMethod.equals(cMethod)) {
                        boolean isSameLine = pMethod.getBegin() == cMethod.getBegin() &&  pMethod.getEnd() == cMethod.getEnd();
                        boolean isSameContent = pMethod.getContent().equals(cMethod.getContent());
                        if (!isSameLine || !isSameContent) {
                            physicalChangedHandler.subTreeMapping(preNode, node, commonInfo, proxyDao);
                        }
                        preSet.remove(preNode);
                        break;
                    }
                }
                continue;
            }

            if (node instanceof ClassNode) {
                for (BaseNode preNode : preSet) {
                    ClassNode pClass = (ClassNode)preNode;
                    ClassNode cClass = (ClassNode)node;
                    if (pClass.equals(cClass)) {
                        NodeMapping.setNodeMapped(preNode, node, proxyDao, commonInfo);
                        preSet.remove(preNode);
                        break;
                    }
                }

            }

            if (node instanceof FieldNode) {
                for (BaseNode preNode : preSet) {
                    FieldNode pField = (FieldNode)preNode;
                    FieldNode cField = (FieldNode)node;
                    if (pField.equals(cField)) {
                        NodeMapping.setNodeMapped(preNode,node,proxyDao,commonInfo);
                        preSet.remove(preNode);
                        break;
                    }
                }
            }
        }

    }


    private void dealWithAddDelete(BaseNode baseNode, DiffInfo diffInfo, NodeMapping nodeMapping, Set<BaseNode> nodeSet, ProxyDao proxyDao) {
        if (baseNode instanceof StatementNode) {
            ((StatementNode) baseNode).setDescription(diffInfo.getDescription());
        }
        if (baseNode instanceof MethodNode) {
            ((MethodNode) baseNode).setDiff(diffInfo.getJsonObject());
        }
        if (nodeMapping instanceof AddHandler) {
            nodeMapping.subTreeMapping(null, baseNode,commonInfo,proxyDao);
        }else {
            nodeMapping.subTreeMapping(baseNode, null,commonInfo,proxyDao);
        }

        BaseNode tmp = baseNode;
        backTracing(tmp);
        nodeSet.remove(baseNode);
    }

    private void backTracing(BaseNode baseNode) {
        while (baseNode.getParent() != null) {
            BaseNode parent = baseNode.getParent();
            if (parent.getChangeStatus().getPriority() > BaseNode.ChangeStatus.CHANGE.getPriority()) {
                parent.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
            }
            baseNode = parent;
        }
    }

    private Map<ProjectInfoLevel, Set<BaseNode>> extractMapFromNode(BaseNode root) {
        Map<ProjectInfoLevel, Set<BaseNode>> map = new HashMap<>(8);
        map.put(ProjectInfoLevel.CLASS, new HashSet<>());
        map.put(ProjectInfoLevel.METHOD, new HashSet<>());
        map.put(ProjectInfoLevel.FIELD, new HashSet<>());
        map.put(ProjectInfoLevel.STATEMENT, new HashSet<>());

        Queue<BaseNode> queue = new ArrayDeque<>();
        queue.offer(root);
        while (queue.size() != 0) {
            BaseNode baseNode = queue.poll();
            if(baseNode instanceof FieldNode) {
                continue;
            }
            if (baseNode.getChildren() != null) {
                ProjectInfoLevel key = baseNode.getChildren().get(0).getProjectInfoLevel();
                map.get(key).addAll(baseNode.getChildren());
                queue.addAll(baseNode.getChildren());
            }
            if (baseNode instanceof ClassNode) {
                ClassNode classNode = (ClassNode)baseNode;
                map.get(ProjectInfoLevel.FIELD).addAll(classNode.getFieldNodes());
                queue.addAll(classNode.getFieldNodes());
            }
        }
        return map;
    }


    public void setMapThreadLocal(Map<String, List<DiffInfo>> map) {
        mapThreadLocal.remove();
        mapThreadLocal.set(map);
    }

}