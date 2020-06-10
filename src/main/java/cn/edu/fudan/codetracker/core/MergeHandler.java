package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.core.tree.JavaTree;
import cn.edu.fudan.codetracker.core.tree.Language;
import cn.edu.fudan.codetracker.core.tree.RepoInfoTree;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.*;

public class MergeHandler implements PublicConstants {
    private MergeHandler(){}

    public static MergeHandler getInstance(){
        return MappingGeneratorHolder.MERGE_HANDLER;
    }

    private static final class MappingGeneratorHolder {
        static final MergeHandler MERGE_HANDLER = new MergeHandler();
    }

    public JSONObject dealWithMerge(JGitHelper jGitHelper, String commit, String outputPath, String repoUuid, String branch, Map<String,Map<String,String>> logicalChangedFileMap) {
        Map<String, List<DiffEntry>> conflictInfo = jGitHelper.getConflictDiffEntryList(commit);
        if (conflictInfo == null || conflictInfo.size() == 0) {
            return null;
        }
        String str = null;
        List<DiffEntry> list = null;
        for (Map.Entry<String,List<DiffEntry>> entry: conflictInfo.entrySet()) {
            str = entry.getKey();
            list = entry.getValue();
        }
        if (str == null || list == null || list.size() == 0) {
            return null;
        }

        //获取parent1，parent2；parent1和parent2是通过比对提交者和时间确定的顺序
        String[] parents = str.split(":");
        String parent1 = parents[0];
        String parent2 = parents[1];

        CommonInfo preCommonInfo = constructCommonInfo(repoUuid,branch,parent1,null,jGitHelper);
        CommonInfo curCommonInfo = constructCommonInfo(repoUuid,branch,commit,parent1,jGitHelper);
        CommonInfo compareCommonInfo = constructCommonInfo(repoUuid,branch,parent2,null,jGitHelper);

        //目前策略：处理conflict，首先由parent1与current进行对比，若有add情况，去parent2中寻找有无对应节点
        //构造三棵树
        List<String> fileList = new ArrayList<>();
        for (DiffEntry diffEntry : list) {
            if ("MODIFY".equals(diffEntry.getChangeType())) {
                fileList.add(diffEntry.getNewPath());
            }
        }
        Map<String,List<String>> map = new HashMap<>();
        map.put("CHANGE",fileList);
        jGitHelper.checkout(parent1);
        RepoInfoTree preTree = new RepoInfoTree(fileList,preCommonInfo,repoUuid);
        jGitHelper.checkout(commit);
        RepoInfoTree curTree = new RepoInfoTree(fileList,curCommonInfo,repoUuid);
        jGitHelper.checkout(parent2);
        RepoInfoTree compareTree = new RepoInfoTree(fileList,compareCommonInfo,repoUuid);

        JavaTree preJavaTree = (JavaTree) preTree.getRepoTree().get(Language.JAVA);
        JavaTree curJavaTree = (JavaTree) curTree.getRepoTree().get(Language.JAVA);
        JavaTree compareJavaTree = (JavaTree) compareTree.getRepoTree().get(Language.JAVA);

        TrackerCore.mapping(preJavaTree,curJavaTree,preCommonInfo,repoUuid,branch,map,logicalChangedFileMap,outputPath,parent1);

        //与第三棵树对比ADD情况
        compareWithParent2(curJavaTree,compareJavaTree);

        JSONObject result = new JSONObject();
        result.put("pre",preTree);
        result.put("cur",curTree);
        result.put("commonInfo",curCommonInfo);

        return result;

    }

    private void compareWithParent2(JavaTree curTree, JavaTree compareTree) {
        if (curTree == null || compareTree == null) {
            return;
        }
        for (ClassNode classNode : curTree.getClassInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(classNode.getChangeStatus())) {
                if (findSameNode(classNode,compareTree) != null) {
                    classNode.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                }
            }
        }

        for (MethodNode methodNode : curTree.getMethodInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(methodNode.getChangeStatus())) {
                if (findSameNode(methodNode,compareTree) != null) {
                    methodNode.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                }
            }
        }

        for (FieldNode fieldNode : curTree.getFieldInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(fieldNode.getChangeStatus())) {
                if (findSameNode(fieldNode,compareTree) != null) {
                    fieldNode.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                }
            }
        }

        for (StatementNode statementNode : curTree.getStatementInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(statementNode.getChangeStatus())) {
                if (findSameNode(statementNode,compareTree) != null) {
                    statementNode.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                }
            }
        }

    }

    private BaseNode findSameNode(BaseNode baseNode, JavaTree compareTree) {
        if(baseNode instanceof ClassNode) {
            ClassNode classNode = (ClassNode)baseNode;
            for (ClassNode node : compareTree.getClassInfos()) {
                if (classNode.equals(node)) {
                    return node;
                }
            }
            return null;
        }
        if(baseNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode)baseNode;
            for (MethodNode node : compareTree.getMethodInfos()) {
                if (methodNode.equals(node)) {
                    return node;
                }
            }
            return null;
        }
        if(baseNode instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode)baseNode;
            for (FieldNode node : compareTree.getFieldInfos()) {
                if (fieldNode.equals(node)) {
                    return node;
                }
            }
            return null;
        }
        if(baseNode instanceof StatementNode) {
            StatementNode statementNode = (StatementNode)baseNode;
            BaseNode root = findSameNode(statementNode.getParent(),compareTree);
            if (root == null) {
                return null;
            } else {
                for (BaseNode node : root.getChildren()) {
                    StatementNode statement = (StatementNode)node;
                    if (statement.equals(statementNode)) {
                        return statement;
                    }
                }
                return null;
            }
        }
        return null;
    }

    private CommonInfo constructCommonInfo(String repoUuid, String branch, String commit, String parentCommit, JGitHelper jGitHelper) {
        if (parentCommit == null || parentCommit.length() == 0) {
            parentCommit = commit;
        }
        jGitHelper.checkout(commit);

        Date commitDate = getDateByString(jGitHelper.getCommitTime(commit));
        String committer = jGitHelper.getAuthorName(commit);
        String commitMessage = jGitHelper.getMess(commit);
        // String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit
        return new CommonInfo(repoUuid, branch, commit, commitDate, committer, commitMessage, parentCommit);
    }
}
