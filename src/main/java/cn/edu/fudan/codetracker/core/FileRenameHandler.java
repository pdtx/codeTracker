package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.core.tree.JavaTree;
import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.FileFilter;
import cn.edu.fudan.codetracker.util.comparison.CosineUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * todo 可以根据jgitDiff的行号来进行匹配改进
 * description: 处理文件重命名的情况
 *
 * @author fancying
 * create: 2020-06-09 22:22
 **/
@Slf4j
class FileRenameHandler implements PublicConstants {

    private static PhysicalChangedHandler physicalChangedHandler = PhysicalChangedHandler.getInstance();

    static void dealWithRename(List<String> renameList, JavaTree preRepoInfo, JavaTree curRepoInfo, CommonInfo commonInfo, ProxyDao proxyDao) {

        // 预处理
        // key: prePath value:curPath
        Map<String, String> renameMap = new LinkedHashMap<>();
        for (String str: renameList) {
            String[] paths = str.split(DELIMITER_RENAME);
            renameMap.put(paths[0], paths[1]);
        }
        int renameInitialCapacity = renameMap.size() << 1;
        Map<String, FileNode> preRenameMap = new HashMap<>(renameInitialCapacity);
        Map<String, FileNode> curRenameMap = new HashMap<>(renameInitialCapacity);
        preRepoInfo.getFileInfos().stream().
                filter(f -> renameMap.keySet().contains(f.getFilePath())).
                forEach(f -> preRenameMap.put(f.getFilePath(), f));
        curRepoInfo.getFileInfos().stream().
                filter(f -> renameMap.values().contains(f.getFilePath())).
                forEach(f -> curRenameMap.put(f.getFilePath(),f));


        // 遍历rename的map 匹配两个fileNode
        for (Map.Entry<String, String> entry : renameMap.entrySet()) {
            if (FileFilter.javaFilenameFilter(entry.getKey())) {
                continue;
            }
            FileNode preFileNode = preRenameMap.get(entry.getKey());
            FileNode curFileNode = curRenameMap.get(entry.getValue());
            curFileNode.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
            NodeMapping.setNodeMapped(preFileNode, curFileNode, proxyDao, commonInfo);

            // 处理file下的class node
//            renameClassMapping(preFileNode, curFileNode, proxyDao, commonInfo);
            //阈值100 处理file下面节点
            dealWithOnlyRenameSituation(preFileNode, curFileNode, proxyDao, commonInfo);
        }

    }

    /**
     * fixme 只考虑rename无内容改变，更新file、class、method、field的meta_info，便于查找trackerInfo
     * @param preFileNode
     * @param curFileNode
     * @param proxyDao
     * @param commonInfo
     */
    private static void dealWithOnlyRenameSituation(FileNode preFileNode, FileNode curFileNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        // 先完成重命名的匹配
        ClassNode preClassNode = findClassNode(preFileNode);
        ClassNode curClassNode = findClassNode(curFileNode);
        if (curClassNode == null || preClassNode == null) {
            return;
        }
        curClassNode.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
        NodeMapping.setNodeMapped(preClassNode, curClassNode, proxyDao, commonInfo);
        dealWithUnchangedMethod(preClassNode, curClassNode, proxyDao, commonInfo);
        dealWithUnchangedField(preClassNode, curClassNode, proxyDao, commonInfo);

        //不变class
        for (BaseNode baseNode : curFileNode.getChildren()) {
            if (baseNode.isMapping()) {
                continue;
            }
            ClassNode cClassNode = (ClassNode) baseNode;
            for (BaseNode pBaseNode : preFileNode.getChildren()) {
                if (pBaseNode.isMapping()) {
                    continue;
                }
                ClassNode pClassNode = (ClassNode) pBaseNode;
                if (cClassNode.getClassName().equals(pClassNode.getClassName())) {
                    NodeMapping.setNodeMapped(pClassNode, cClassNode, proxyDao, commonInfo);
                    if (cClassNode.getVersion() > 1) {
                        cClassNode.setVersion(cClassNode.getVersion() - 1);
                    }
                    dealWithUnchangedMethod(pClassNode, cClassNode, proxyDao, commonInfo);
                    dealWithUnchangedField(pClassNode, cClassNode, proxyDao, commonInfo);
                    break;
                }
            }
        }
    }

    private static void dealWithUnchangedMethod(ClassNode preClassNode, ClassNode curClassNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        for (BaseNode baseNode : curClassNode.getChildren()) {
            if (baseNode.isMapping()) {
                continue;
            }
            MethodNode curMethodNode = (MethodNode) baseNode;
            for (BaseNode pBaseNode : preClassNode.getChildren()) {
                if (pBaseNode.isMapping()) {
                    continue;
                }
                MethodNode preMethodNode = (MethodNode) pBaseNode;
                if (curMethodNode.getSignature().equals(preMethodNode.getSignature())) {
                    NodeMapping.setNodeMapped(preMethodNode, curMethodNode, proxyDao, commonInfo);
                    if (curMethodNode.getVersion() > 1) {
                        curMethodNode.setVersion(curMethodNode.getVersion() - 1);
                    }
                    break;
                }
            }
        }
    }

    private static void dealWithUnchangedField(ClassNode preClassNode, ClassNode curClassNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        for (BaseNode baseNode : curClassNode.getFieldNodes()) {
            if (baseNode.isMapping()) {
                continue;
            }
            FieldNode curFieldNode = (FieldNode) baseNode;
            for (BaseNode pBaseNode : preClassNode.getFieldNodes()) {
                if (pBaseNode.isMapping()) {
                    continue;
                }
                FieldNode preFieldNode = (FieldNode) pBaseNode;
                if (curFieldNode.isSame(preFieldNode)) {
                    NodeMapping.setNodeMapped(preFieldNode, curFieldNode, proxyDao, commonInfo);
                    if (curFieldNode.getVersion() > 1) {
                        curFieldNode.setVersion(curFieldNode.getVersion() - 1);
                    }
                    break;
                }
            }
        }
    }

    private static void renameClassMapping(FileNode preFileNode, FileNode curFileNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        // 先完成重命名的匹配
        ClassNode preClassNode = findClassNode(preFileNode);
        ClassNode curClassNode = findClassNode(curFileNode);
        curClassNode.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
        NodeMapping.setNodeMapped(preClassNode, curClassNode, proxyDao, commonInfo);
        
        method(preClassNode, curClassNode, proxyDao, commonInfo);
        field(preClassNode, curClassNode, proxyDao, commonInfo);
        
        
        //接下来的非重命名的匹配  遍历该文件下的所有class做mapping
        for (BaseNode baseNode : preFileNode.getChildren()) {
            if (baseNode.isMapping()) {
                continue;
            }
            preClassNode = (ClassNode)baseNode;
            for (BaseNode cbaseNode : curFileNode.getChildren()) {
                if (cbaseNode.isMapping()) {
                    continue;
                }
                curClassNode = (ClassNode)cbaseNode;
                if (preClassNode.getClassName().equals(curClassNode.getClassName())) {

                    // fixme 该class 可能没变
                    curClassNode.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
                    NodeMapping.setNodeMapped(preClassNode, curClassNode, proxyDao, commonInfo);
                    method(preClassNode, curClassNode, proxyDao, commonInfo);
                    field(preClassNode, curClassNode, proxyDao, commonInfo);
                    break;
                }
            }
            if (! baseNode.isMapping()){
                NodeMapping.subTreeMappingRecursive(preClassNode, commonInfo ,proxyDao, BaseNode.ChangeStatus.DELETE);
            }
        }

        curFileNode.getChildren().stream().
                filter(c -> !c.isMapping()).
                forEach(c -> NodeMapping.subTreeMappingRecursive(c, commonInfo ,proxyDao, BaseNode.ChangeStatus.ADD));

    }

    private static void field(ClassNode preClassNode, ClassNode curClassNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        List<FieldNode> preFieldNodeList = preClassNode.getFieldNodes();
        List<FieldNode> curFieldNodeList = curClassNode.getFieldNodes();
        for (FieldNode pFieldNode : preFieldNodeList) {
            if (pFieldNode.isMapping()) {
                continue;
            }

            for (FieldNode cFieldNode : curFieldNodeList) {
                boolean isSameFullName = cFieldNode.getFullName().equals(pFieldNode.getFullName());
                if ( !cFieldNode.isMapping() && cFieldNode.getSimpleName().equals(pFieldNode.getSimpleName()) ) {
                    if (isSameFullName) {
                        cFieldNode.setMapping(true);
                        pFieldNode.setMapping(true);
                        break;
                    }
                    cFieldNode.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
                    NodeMapping.setNodeMapped(pFieldNode, cFieldNode, proxyDao, commonInfo);
                    break;
                }

            }
            if (!pFieldNode.isMapping()){
                NodeMapping.subTreeMappingRecursive(pFieldNode, commonInfo ,proxyDao, BaseNode.ChangeStatus.DELETE);
            }

        }
        // 处理没匹配上的为add
        curFieldNodeList.stream().
                filter(c -> !c.isMapping()).
                forEach(c -> NodeMapping.subTreeMappingRecursive(c, commonInfo ,proxyDao, BaseNode.ChangeStatus.ADD));

    }

    @SuppressWarnings("unchecked")
    private static void method(ClassNode preClassNode, ClassNode curClassNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        List<MethodNode> preMethodNodeList = (List<MethodNode>) preClassNode.getChildren();
        List<MethodNode> curMethodNodeList = (List<MethodNode>) curClassNode.getChildren();
        for (MethodNode pMethodNode : preMethodNodeList) {
            if (pMethodNode.isMapping()) {
                continue;
            }
            // key methodNode value similarity
            Map<MethodNode, Double> cMethodMap = MethodNode.findMostSimilarMethod(pMethodNode, curMethodNodeList);
            // 找到之后还需要判断是物理上的改变还是逻辑上的改变
            if (cMethodMap.isEmpty()) {
                NodeMapping.subTreeMappingRecursive(pMethodNode, commonInfo ,proxyDao, BaseNode.ChangeStatus.DELETE);
                continue;
            }
            MethodNode cMethodNode = (MethodNode)cMethodMap.keySet().toArray()[0];
            double similarity = cMethodMap.get(cMethodNode);
            // 含有逻辑上的变更或者signature变化
            if (similarity < 1.00) {
                BaseNode.ChangeStatus status = cMethodNode.getFullName().equals(pMethodNode.getFullName())  ? BaseNode.ChangeStatus.CHANGE : BaseNode.ChangeStatus.SELF_CHANGE;
                cMethodNode.setChangeStatus(status);
                NodeMapping.setNodeMapped(pMethodNode, cMethodNode, proxyDao, commonInfo);

                // todo 循环识别 语句的物理和逻辑变更
                statement(pMethodNode, cMethodNode, proxyDao, commonInfo);
                continue;
            }


            //全部匹配上 考虑是否有物理上的变更
            boolean isSameLine = pMethodNode.getBegin() == cMethodNode.getBegin() &&  pMethodNode.getEnd() == cMethodNode.getEnd();
            boolean isSameContent = pMethodNode.getContent().equals(cMethodNode.getContent());
            BaseNode.ChangeStatus status = isSameContent ? BaseNode.ChangeStatus.CHANGE_LINE : BaseNode.ChangeStatus.CHANGE_RECORD;
            if (!isSameLine || !isSameContent) {
                cMethodNode.setChangeStatus(status);
                physicalChangedHandler.subTreeMapping(pMethodNode, cMethodNode, commonInfo, proxyDao);
                // todo 循环识别 语句是否有物理上的变更
                statement(pMethodNode, cMethodNode, proxyDao, commonInfo);
            }

            pMethodNode.setMapping(true);
            cMethodNode.setMapping(true);
        }

        curMethodNodeList.stream().
                filter(c -> !c.isMapping()).
                forEach(c -> NodeMapping.subTreeMappingRecursive(c, commonInfo ,proxyDao, BaseNode.ChangeStatus.ADD));

    }

    @SuppressWarnings("unchecked")
    private static void statement(MethodNode pMethodNode, MethodNode cMethodNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        List<StatementNode> pStatementNodes = (List<StatementNode>)pMethodNode.getChildren();
        List<StatementNode> cStatementNodes = (List<StatementNode>)cMethodNode.getChildren();

        if (pStatementNodes != null){
            for (StatementNode pStatementNode : pStatementNodes) {
                if (pStatementNode.isMapping()) {
                    continue;
                }
                // key StatementNode value similarity
                Map<StatementNode, Double>  map = StatementNode.findMostSimilarStatement(pStatementNode, cStatementNodes);

                // 找到之后还需要判断是物理上的改变还是逻辑上的改变
                if (map.isEmpty()) {
                    NodeMapping.subTreeMappingRecursive(pStatementNode, commonInfo ,proxyDao, BaseNode.ChangeStatus.DELETE);
                    continue;
                }
                StatementNode cStatementNode = (StatementNode)map.keySet().toArray()[0];
                double similarity = map.get(cStatementNode);
                // 含有逻辑上的变更
                if (similarity < 1.00) {
                    // 如果有孩子 判断除去body之外的相似度是否为 1.0
                    BaseNode.ChangeStatus status = (pStatementNode.getChildren() != null &&
                            !((Double)1.00).equals(CosineUtil.cosineSimilarity(pStatementNode.getSelfBodyToken(), cStatementNode.getSelfBodyToken()))) ?
                            BaseNode.ChangeStatus.SELF_CHANGE : BaseNode.ChangeStatus.CHANGE;
                    cStatementNode.setChangeStatus(status);
                    NodeMapping.setNodeMapped(pStatementNode, cStatementNode, proxyDao, commonInfo);
                    continue;
                }

                //全部匹配上 考虑是否有物理上的变更
                boolean isSameLine = pStatementNode.getBegin() == cStatementNode.getBegin() &&  pStatementNode.getEnd() == cStatementNode.getEnd();
                boolean isSameContent = pStatementNode.getBody().equals(cStatementNode.getBody());
                BaseNode.ChangeStatus status = isSameContent ? BaseNode.ChangeStatus.CHANGE_LINE : BaseNode.ChangeStatus.CHANGE_RECORD;
                if (!isSameLine || !isSameContent) {
                    pStatementNode.setChangeStatus(status);
                    physicalChangedHandler.subTreeMapping(pStatementNode, cStatementNode, commonInfo, proxyDao);
                }

                pStatementNode.setMapping(true);
                cStatementNode.setMapping(true);
            }for (StatementNode pStatementNode : pStatementNodes) {
                if (pStatementNode.isMapping()) {
                    continue;
                }
                // key StatementNode value similarity
                Map<StatementNode, Double>  map = StatementNode.findMostSimilarStatement(pStatementNode, cStatementNodes);

                // 找到之后还需要判断是物理上的改变还是逻辑上的改变
                if (map.isEmpty()) {
                    NodeMapping.subTreeMappingRecursive(pStatementNode, commonInfo ,proxyDao, BaseNode.ChangeStatus.DELETE);
                    continue;
                }
                StatementNode cStatementNode = (StatementNode)map.keySet().toArray()[0];
                double similarity = map.get(cStatementNode);
                // 含有逻辑上的变更
                if (similarity < 1.00) {
                    // 如果有孩子 判断除去body之外的相似度是否为 1.0
                    BaseNode.ChangeStatus status = (pStatementNode.getChildren() != null &&
                            !((Double)1.00).equals(CosineUtil.cosineSimilarity(pStatementNode.getSelfBodyToken(), cStatementNode.getSelfBodyToken()))) ?
                            BaseNode.ChangeStatus.SELF_CHANGE : BaseNode.ChangeStatus.CHANGE;
                    cStatementNode.setChangeStatus(status);
                    NodeMapping.setNodeMapped(pStatementNode, cStatementNode, proxyDao, commonInfo);
                    continue;
                }

                //全部匹配上 考虑是否有物理上的变更
                boolean isSameLine = pStatementNode.getBegin() == cStatementNode.getBegin() &&  pStatementNode.getEnd() == cStatementNode.getEnd();
                boolean isSameContent = pStatementNode.getBody().equals(cStatementNode.getBody());
                BaseNode.ChangeStatus status = isSameContent ? BaseNode.ChangeStatus.CHANGE_LINE : BaseNode.ChangeStatus.CHANGE_RECORD;
                if (!isSameLine || !isSameContent) {
                    pStatementNode.setChangeStatus(status);
                    physicalChangedHandler.subTreeMapping(pStatementNode, cStatementNode, commonInfo, proxyDao);
                }

                pStatementNode.setMapping(true);
                cStatementNode.setMapping(true);
            }
        }

        if (cStatementNodes != null) {
            cStatementNodes.stream().
                    filter(c -> !c.isMapping()).
                    forEach(c -> NodeMapping.subTreeMappingRecursive(c, commonInfo ,proxyDao, BaseNode.ChangeStatus.ADD));
        }

    }


    private static ClassNode findClassNode(FileNode fileNode) {
        String[] p = fileNode.getFileName().split("/");
        String name =  p[p.length - 1].replace(".java", "");
        
        
        for (BaseNode baseNode : fileNode.getChildren()) {
            ClassNode classNode = (ClassNode)baseNode;
            if (classNode.getClassName().equals(name)) {
                return classNode;
            }
        }
        log.warn("can not find rename class");
        if (fileNode.getChildren().size() == 0) {
            return null;
        }
        return (ClassNode)fileNode.getChildren().get(0);
    }


}