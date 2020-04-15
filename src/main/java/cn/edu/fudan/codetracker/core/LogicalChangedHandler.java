package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.diff.DiffInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ser.Serializers;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * description: 增加的文件映射
 *
 * @author fancying
 * create: 2020-03-20 19:27
 **/
@Slf4j
public class LogicalChangedHandler implements NodeMapping {

    private String diffPath;

    private Map<String, List<DiffInfo>> map;

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
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot) {
        extractFromDiff();
        if (preRoot instanceof FileNode && curRoot instanceof FileNode) {
            curRoot.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
            NodeMapping.setNodeMapped(preRoot,curRoot);

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
                mapping(preSet,curSet,diffSet);
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
                mapping(preStatements,statements,statementDiffs);
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
                set.add(baseNode);
            }
            for (BaseNode child: baseNode.getChildren()) {
                queue.offer(child);
            }
        }
        return set;
    }

    public void mapping(Set<BaseNode> preSet, Set<BaseNode> curSet, Set<DiffInfo> diffSet) {
        //先匹配diffInfo
        for (DiffInfo diffInfo : diffSet) {
            BaseNode pre = diffInfo.findChangeNode(preSet,false);
            BaseNode cur = diffInfo.findChangeNode(curSet,true);
            if (pre == null && cur == null) {
                continue;
            } else if (pre == null) {
                addHandler.subTreeMapping(null,cur);
                curSet.remove(cur);
            } else if (cur == null) {
                deleteHandler.subTreeMapping(pre,null);
                preSet.remove(pre);
            } else {
                NodeMapping.setNodeMapped(pre,cur);
                switch (diffInfo.getChangeRelation()) {
                    case "Change":
                        cur.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
                        break;
                    case "Move":
                        cur.setChangeStatus(BaseNode.ChangeStatus.MOVE);
                        break;
                    default:
                        break;
                }
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
                                physicalChangedHandler.subTreeMapping(preNode,node);
                            } else {
                                NodeMapping.setNodeMapped(preNode,node);
                                node.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                            }
                            preSet.remove(preNode);
                            break;
                        }
                    } else {
                        StatementNode pStatement = (StatementNode) preNode;
                        StatementNode cStatement = (StatementNode) node;
                        if (pStatement.equals(cStatement)) {
                            if (pStatement.getBegin() != cStatement.getBegin() || pStatement.getEnd() != cStatement.getEnd()) {
                                physicalChangedHandler.subTreeMapping(preNode,node);
                            } else {
                                NodeMapping.setNodeMapped(preNode,node);
                                node.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                            }
                            preSet.remove(preNode);
                            break;
                        }
                    }
                }
            } else {
                break;
            }
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
            Set<BaseNode> set = map.get(key);
            for (BaseNode child: baseNode.getChildren()) {
                set.add(child);
                queue.offer(child);
            }
            map.put(key,set);
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
        map = new HashMap<>();
        map.put("class",new ArrayList<>());
        map.put("method",new ArrayList<>());
        map.put("field",new ArrayList<>());
        map.put("statement",new ArrayList<>());
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
            List<DiffInfo> list = map.get(diffInfo.getType());
            list.add(diffInfo);
            map.put(diffInfo.getType(),list);
        }
    }

    public void setDiffPath(String diffPath) {
        this.diffPath = diffPath;
    }
}