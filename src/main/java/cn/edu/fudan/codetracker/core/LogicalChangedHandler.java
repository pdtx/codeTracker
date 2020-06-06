package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.diff.DiffInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.google.gson.internal.$Gson$Preconditions;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * description: 逻辑上的改变映射
 *
 * @author fancying
 * create: 2020-03-20 19:27
 **/
@Slf4j
public class LogicalChangedHandler implements NodeMapping {
    private String diffPath;

    private Map<String, List<DiffInfo>> map;

    private CommonInfo commonInfo;

    private LogicalChangedHandler(){}

    public static LogicalChangedHandler getInstance(){
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
        extractFromDiff();
        if (preRoot instanceof FileNode && curRoot instanceof FileNode) {
            curRoot.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
            NodeMapping.setNodeMapped(preRoot,curRoot,proxyDao,commonInfo);

            //抽取preMap和curMap
            Map<String, Set<BaseNode>> preMap = extractMapFromNode(preRoot);
            Map<String, Set<BaseNode>> curMap = extractMapFromNode(curRoot);

            String[] keys = {"class","method","field"};

            //先mapping class、method、field
            for (int i = 0; i < keys.length ; i++) {
                String key = keys[i];
                Set<BaseNode> preSet = preMap.get(key);
                Set<BaseNode> curSet = curMap.get(key);
                Set<DiffInfo> diffSet = new HashSet<>(map.get(key));
                mapping(preSet,curSet,diffSet,proxyDao);
            }

            //再mapping属于同一method的statement
            Set<DiffInfo> statementDiffs = new HashSet<>(map.get("statement"));
            for (BaseNode node: curMap.get("method")) {
                MethodNode methodNode = (MethodNode)node;
                Set<BaseNode> statements = getStatementNodeFromMethod(methodNode);
                Set<BaseNode> preStatements = null;
                if(methodNode.getPreMappingBaseNode() != null) {
                    MethodNode preMethodNode = (MethodNode)methodNode.getPreMappingBaseNode();
                    preStatements = getStatementNodeFromMethod(preMethodNode);
                }
                mapping(preStatements,statements,statementDiffs,proxyDao);
            }

        }
    }

    public Set<BaseNode> getStatementNodeFromMethod(MethodNode methodNode) {
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

    public void mapping(Set<BaseNode> preSet, Set<BaseNode> curSet, Set<DiffInfo> diffSet, ProxyDao proxyDao) {
        //先匹配diffInfo
        for (DiffInfo diffInfo : diffSet) {
            BaseNode pre = diffInfo.findChangeNode(preSet,false);
            BaseNode cur = diffInfo.findChangeNode(curSet,true);
            if (pre == null && cur == null) {
                continue;
            } else if (pre == null) {
                if (cur instanceof StatementNode) {
                    ((StatementNode) cur).setDescription(diffInfo.getDescription());
                }
                if (cur instanceof MethodNode) {
                    MethodNode methodNode = (MethodNode)cur;
                    methodNode.setDiff(diffInfo.getJsonObject());
                }
                addHandler.subTreeMapping(null,cur,commonInfo,proxyDao);
                BaseNode tmp = cur;
                backTracing(tmp);
                curSet.remove(cur);
            } else if (cur == null) {
                if (pre instanceof StatementNode) {
                    ((StatementNode) pre).setDescription(diffInfo.getDescription());
                }
                if (pre instanceof MethodNode) {
                    MethodNode methodNode = (MethodNode)pre;
                    methodNode.setDiff(diffInfo.getJsonObject());
                }
                deleteHandler.subTreeMapping(pre,null,commonInfo,proxyDao);
                BaseNode tmp = pre;
                backTracing(tmp);
                preSet.remove(pre);
            } else {
                if (cur instanceof StatementNode) {
                    ((StatementNode) cur).setDescription(diffInfo.getDescription());
                }
                if (cur instanceof MethodNode) {
                    MethodNode methodNode = (MethodNode)cur;
                    methodNode.setDiff(diffInfo.getJsonObject());
                }
                BaseNode tmp;
                switch (diffInfo.getChangeRelation()) {
                    case "Change":
                        cur.setChangeStatus(BaseNode.ChangeStatus.SELF_CHANGE);
                        tmp = cur;
                        backTracing(tmp);
                        break;
                    case "Move":
                        cur.setChangeStatus(BaseNode.ChangeStatus.MOVE);
                        tmp = cur;
                        backTracing(tmp);
                        break;
                    default:
                        break;
                }
                NodeMapping.setNodeMapped(pre,cur,proxyDao,commonInfo);
                preSet.remove(pre);
                curSet.remove(cur);
            }
        }

        if (curSet == null || preSet == null) {
            return;
        }

        //再遍历剩下的（不变或者物理改变）
        for (BaseNode node: curSet) {
            if (node instanceof MethodNode || node instanceof StatementNode) {
                for (BaseNode preNode: preSet) {
                    if (node instanceof MethodNode) {
                        MethodNode pMethod = (MethodNode)preNode;
                        MethodNode cMethod = (MethodNode)node;
                        if (pMethod.equals(cMethod)) {
                            if (pMethod.getBegin() != cMethod.getBegin() || pMethod.getEnd() != cMethod.getEnd()) {
                                physicalChangedHandler.subTreeMapping(preNode,node,commonInfo,proxyDao);
                            } else {
                                node.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                                NodeMapping.setNodeMapped(preNode,node,proxyDao,commonInfo);
                            }
                            preSet.remove(preNode);
                            break;
                        }
                    } else {
                        StatementNode pStatement = (StatementNode) preNode;
                        StatementNode cStatement = (StatementNode) node;
                        if (pStatement.equals(cStatement)) {
                            if (pStatement.getBegin() != cStatement.getBegin() || pStatement.getEnd() != cStatement.getEnd()) {
                                physicalChangedHandler.subTreeMapping(preNode,node,commonInfo,proxyDao);
                            } else {
                                node.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                                NodeMapping.setNodeMapped(preNode,node,proxyDao,commonInfo);
                            }
                            preSet.remove(preNode);
                            break;
                        }
                    }
                }
            } else {
                for (BaseNode preNode : preSet) {
                    if (node instanceof ClassNode) {
                        ClassNode pClass = (ClassNode)preNode;
                        ClassNode cClass = (ClassNode)node;
                        if (pClass.equals(cClass)) {
                            NodeMapping.setNodeMapped(preNode,node,proxyDao,commonInfo);
                            preSet.remove(preNode);
                            break;
                        }
                    }
                    if (node instanceof FieldNode) {
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

    }

    public void backTracing(BaseNode baseNode) {
        while (baseNode.getParent() != null) {
            BaseNode parent = baseNode.getParent();
            if (parent.getChangeStatus().getPriority() > BaseNode.ChangeStatus.CHANGE.getPriority()) {
                parent.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
            }
            baseNode = parent;
        }
    }

    public Map<String,Set<BaseNode>> extractMapFromNode(BaseNode root) {
        Map<String, Set<BaseNode>> map = new HashMap<>();
        map.put("class",new HashSet<>());
        map.put("method",new HashSet<>());
        map.put("field",new HashSet<>());
        map.put("statement",new HashSet<>());
        Queue<BaseNode> queue = new ArrayDeque<>();
        queue.offer(root);
        while (queue.size()!=0) {
            BaseNode baseNode = queue.poll();
            if(baseNode instanceof FieldNode) {
                continue;
            }
            String key = null;
            if (baseNode instanceof FileNode) {
                key = "class";
            } else if (baseNode instanceof ClassNode) {
                key = "method";
            } else if(baseNode instanceof MethodNode) {
                key = "statement";
            } else if (baseNode instanceof StatementNode) {
                key = "statement";
            }
            if (baseNode.getChildren() != null) {
                Set<BaseNode> set = map.get(key);
                for (BaseNode child: baseNode.getChildren()) {
                    set.add(child);
                    queue.offer(child);
                }
                map.put(key,set);
            }
            if (baseNode instanceof ClassNode) {
                ClassNode classNode = (ClassNode)baseNode;
                Set<BaseNode> s = map.get("field");
                for (BaseNode fieldNode: classNode.getFieldNodes()) {
                    s.add(fieldNode);
                    queue.offer(fieldNode);
                }
                map.put("field",s);
            }
        }
        return map;
    }

    public void extractFromDiff() {
        map = new HashMap<>(8);
        map.put("class", new ArrayList<>());
        map.put("method", new ArrayList<>());
        map.put("field", new ArrayList<>());
        map.put("statement", new ArrayList<>());
        String input;
        try {
            input = FileUtils.readFileToString(new File(diffPath), "UTF-8");
        }catch (IOException e) {
            log.error(e.getMessage());
            return;
        }
        JSONArray diffDetail = JSONArray.parseArray(input);
        for (int i = 0; i < diffDetail.size() ; i++) {
            JSONObject jsonObject = diffDetail.getJSONObject(i);
            DiffInfo diffInfo = new DiffInfo(jsonObject);
            if (diffInfo.getType() == null) {
                log.error("diff info type error : " + jsonObject.getString("type1") + " ; " + jsonObject.getString("description"));
                continue;
            }
            List<DiffInfo> list = map.get(diffInfo.getType());
            list.add(diffInfo);
            map.put(diffInfo.getType(),list);
        }
    }

    public void setDiffPath(String diffPath) {
        this.diffPath = diffPath;
    }
}