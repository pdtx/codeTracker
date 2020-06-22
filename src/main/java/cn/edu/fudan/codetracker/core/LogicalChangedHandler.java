package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.diff.DiffInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.comparison.CosineUtil;
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
    private Map<ProjectInfoLevel, Set<BaseNode>> preMap;
    private Map<ProjectInfoLevel, Set<BaseNode>> curMap;


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
        if (diffMap == null) {
            log.error("lack diff info");
            return;
        }

        if (preRoot instanceof FileNode && curRoot instanceof FileNode) {
            curRoot.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
            NodeMapping.setNodeMapped(preRoot, curRoot, proxyDao, commonInfo);

            //抽取preMap和curMap
            // key: [class method field statement] value:[BaseNode]
            preMap = extractMapFromNode(preRoot);
            curMap = extractMapFromNode(curRoot);

            ProjectInfoLevel[] keys = {ProjectInfoLevel.CLASS, ProjectInfoLevel.METHOD, ProjectInfoLevel.FIELD};

            //先mapping class、method、field
            // method change signature[self_change]
            for (ProjectInfoLevel p : keys) {
                Set<BaseNode> preSet = new HashSet<>(preMap.get(p));
                Set<BaseNode> curSet = new HashSet<>(curMap.get(p));
                Set<DiffInfo> diffSet = new HashSet<>(diffMap.get(p.getName()));
                mapping(preSet, curSet, diffSet, proxyDao);
            }

            //再mapping属于同一method的statement
            Set<DiffInfo> statementDiffs = new HashSet<>(diffMap.get("statement"));
            for (BaseNode node: curMap.get(ProjectInfoLevel.METHOD)) {
                //方法判定为unchanged，即行号和内容均无变化，其子节点无需再匹配
                if (BaseNode.ChangeStatus.UNCHANGED.equals(node.getChangeStatus())) {
                    continue;
                }
                MethodNode methodNode = (MethodNode)node;
                MethodNode preMethodNode = null;
                Set<BaseNode> statements = getStatementNodeFromMethod(methodNode);
                Set<BaseNode> preStatements = null;
                if(methodNode.getPreMappingBaseNode() != null) {
                    preMethodNode = (MethodNode)methodNode.getPreMappingBaseNode();
                    preStatements = getStatementNodeFromMethod(preMethodNode);
                }
                Set<DiffInfo> relatedDiffInfo = getMethodRelatedDiffInfo(methodNode, preMethodNode,statementDiffs);
                mapping(preStatements, statements, relatedDiffInfo, proxyDao);
            }
        }
    }

    private Set<DiffInfo> getMethodRelatedDiffInfo(MethodNode methodNode, MethodNode preMethodNode, Set<DiffInfo> diffInfoSet) {
        Set<DiffInfo> resultSet = new HashSet<>();
        int methodBegin = methodNode.getBegin();
        int methodEnd = methodNode.getEnd();
        int preBegin = 0, preEnd = 0;
        if (preMethodNode != null) {
            preBegin = preMethodNode.getBegin();
            preEnd = preMethodNode.getEnd();
        }
        for (DiffInfo diffInfo : diffInfoSet) {
            if (diffInfo.isCurMethodRelated(methodBegin, methodEnd) || diffInfo.isPreMethodRelated(preBegin, preEnd)) {
                resultSet.add(diffInfo);
            }
        }
        return resultSet;
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
                dealWithAddDelete(cur, diffInfo, addHandler, curSet, proxyDao, true);
                continue;
            }
            if (cur == null) {
                dealWithAddDelete(pre, diffInfo, deleteHandler, preSet, proxyDao, false);
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
    private StatementNode findMostSimilarStatement(StatementNode target, Set<BaseNode> preSet) {
        StatementNode result = null;
        double similarity = 0.8;

        List<StatementNode> similarStatementList = new ArrayList<>();
        for (BaseNode baseNode : preSet) {
            StatementNode statement = (StatementNode)baseNode;
            if (statement.getLevel() != target.getLevel()) {
                continue;
            }
            double tmp =  CosineUtil.cosineSimilarity(target.getSelfBodyToken(), statement.getSelfBodyToken());

            if (tmp > similarity || ((Double)Double.NaN).equals(tmp)) {
                similarStatementList.add(statement);
            }
        }

        if (similarStatementList.size() == 1) {
            return similarStatementList.get(0);
        }

        double totalBody1 = 0.0;
        double totalBody2 = 0.0;
        String targetBody = CosineUtil.removeComment(target.getBody())  ;
        for (StatementNode statementNode : similarStatementList) {
            String sBody = CosineUtil.removeComment(statementNode.getBody())  ;
            double tmp1 = CosineUtil.cosineSimilarity(targetBody, sBody);
            double tmp2  = CosineUtil.cosineSimilarityWithoutTokenize(targetBody, sBody);
            if (tmp1 >= totalBody1 && tmp2 >= totalBody2) {
                totalBody1 = tmp1;
                totalBody2 = tmp2;
                result = statementNode;
            }
        }

        return result;
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
                        pMethod.setMapping(true);
                        cMethod.setMapping(true);
                        pMethod.setNextMappingBaseNode(cMethod);
                        cMethod.setPreMappingBaseNode(pMethod);
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


    private void dealWithAddDelete(BaseNode baseNode, DiffInfo diffInfo, NodeMapping nodeMapping, Set<BaseNode> nodeSet, ProxyDao proxyDao, boolean isAdd) {
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

        //删除节点
        removeFromSet(baseNode, nodeSet, isAdd);
    }

    private void removeFromSet(BaseNode root, Set<BaseNode> nodeSet, boolean isAdd) {
        // fixme 新增删除子节点是否也为新增删除 已被处理
        Map<ProjectInfoLevel, Set<BaseNode>> map = isAdd ? curMap : preMap;
        Queue<BaseNode> queue = new ArrayDeque<>();
        queue.offer(root);
        while (queue.size() != 0) {
            BaseNode node = queue.poll();
            if (node == root || root instanceof StatementNode) {
                nodeSet.remove(node);
            }
            map.get(node.getProjectInfoLevel()).remove(node);
            if (node.getChildren() != null) {
                for (BaseNode child : node.getChildren()) {
                    queue.offer(child);
                }
            }
            if (node instanceof ClassNode && ((ClassNode) node).getFieldNodes() != null) {
                for (BaseNode field : ((ClassNode) node).getFieldNodes()) {
                    queue.offer(field);
                }
            }
        }
    }

    private void backTracing(BaseNode baseNode) {
        while (baseNode.getParent() != null) {
            BaseNode parent = baseNode.getParent();
            if (parent.getChangeStatus().getPriority() > BaseNode.ChangeStatus.CHANGE.getPriority()) {
                parent.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
                //由于method和statement在无对应diffInfo的情况下会被处理成物理改变，version不变，如果其孩子结点逻辑改变，需要将其version加一
                if (parent instanceof StatementNode || parent instanceof MethodNode) {
                    parent.setVersion(parent.getVersion()+1);
                }
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
            if (baseNode.getChildren() != null && baseNode.getChildren().size() != 0) {
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


    void setMapThreadLocal(Map<String, List<DiffInfo>> map) {
        mapThreadLocal.remove();
        mapThreadLocal.set(map);
    }

}