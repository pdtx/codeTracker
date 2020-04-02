/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 22:26
 **/
package cn.edu.fudan.codetracker.util;


import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class RepoInfoBuilder {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String repoUuid;
    private String commit;
    private String committer;
    private Date commitDate;
    private String commitMessage;
    private String branch;
    /**
     * 最近一次引起变化的commit
     */
    private String parentCommit;
    private JGitHelper jGitHelper;
    private CommonInfo commonInfo;
//    private int importCount = 0;

    private Map<String, List<PackageNode>> moduleInfos;
    private List<PackageNode> packageInfos;
    private List<FileNode> fileInfos;
    private List<ClassNode> classInfos;
    private List<FieldNode> fieldInfos;
    private List<MethodNode> methodInfos;
    private List<StatementNode> statementInfos;

    public RepoInfoBuilder(String repoUuid, String commit, List<String> fileList, JGitHelper jGitHelper, String branch, String parentCommit, List<String> relativePath) {
       constructor(repoUuid, commit, fileList, jGitHelper, branch, parentCommit, relativePath);
    }

    public RepoInfoBuilder(String repoUuid, String commit, String repoPath, JGitHelper jGitHelper, String branch, String parentCommit, List<String> relativePath) {
        File file = new File(repoPath);
        constructor(repoUuid, commit, listJavaFiles(file), jGitHelper, branch, parentCommit, relativePath);
    }

    public RepoInfoBuilder(RepoInfoBuilder repoInfo, List<String> filesList, Boolean isFirst, List<String> relativePath) {
        constructor(repoInfo.getRepoUuid(), repoInfo.getCommit(), filesList, repoInfo.getJGitHelper(), repoInfo.getBranch(), isFirst ? repoInfo.getCommit() : repoInfo.getParentCommit(), relativePath);
    }

    private void constructor(String repoUuid, String commit, List<String> fileList, JGitHelper jGitHelper, String branch, String parentCommit, List<String> relativePath){
        if (relativePath == null || relativePath.size() == 0) {
            relativePath = fileList;
        }
        this.repoUuid = repoUuid;
        this.commit = commit;
        this.branch = branch;
        this.jGitHelper = jGitHelper;
        // first time parentCommit is NULL
        this.parentCommit = parentCommit;
        if (this.parentCommit == null || this.parentCommit.length() == 0) {
            this.parentCommit = commit;
        }
        try{
            commitDate = FORMATTER.parse(jGitHelper.getCommitTime(commit));
            committer = jGitHelper.getAuthorName(commit);
            commitMessage = jGitHelper.getMess(commit);
            // String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit
            commonInfo = new CommonInfo(repoUuid, branch, commit, commitDate, committer, commitMessage, this.parentCommit);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        moduleInfos = new HashMap<>(4);
        packageInfos = new ArrayList<>();
        fileInfos = new ArrayList<>();
        classInfos = new ArrayList<>();
        fieldInfos = new ArrayList<>();
        methodInfos = new ArrayList<>();
        statementInfos = new ArrayList<>();
        analyze(fileList, relativePath);
    }

    private void analyze(List<String> fileList, List<String> relativePath) {
        if (fileList.size() != relativePath.size()) {
            log.error("fileList：{}，relativePath：{} ", fileList.size(), relativePath.size());
        }
        int minSize = Math.min(fileList.size(), relativePath.size());
        // 一个module内包含哪些package
        for (int i = 0; i < minSize ;i++) {
            String path = fileList.get(i);
            String[] strs = path.split("/");
            String str = strs[strs.length-1];
            // 特定文件过滤
            if (FileFilter.filenameFilter(str)) {
                continue;
            }
            FileInfoExtractor fileInfoExtractor = new FileInfoExtractor(path, relativePath.get(i), repoUuid);
            String packageName = fileInfoExtractor.getPackageName();
            // special situation ： end with .java but empty
            if (packageName == null) {
                continue;
            }
            String moduleName = fileInfoExtractor.getModuleName();
            //String moduleName, String packageName
            PackageNode packageNode = new PackageNode(moduleName, packageName);
            if (moduleInfos.containsKey(moduleName)) {
                if (moduleInfos.get(moduleName).contains(packageNode)) {
                    packageNode = findPackageInfoByPackageName(packageNode, moduleInfos.get(moduleName));
                } else {
                    moduleInfos.get(moduleName).add(packageNode);
                }
            } else {
                List<PackageNode> packageInfos = new ArrayList<>();
                packageInfos.add(packageNode);
                moduleInfos.put(moduleName, packageInfos);
            }

            // 设置父节点
            fileInfoExtractor.getFileNode().setParent(packageNode);
            fileInfoExtractor.parseClassInterface();
            packageNode.getFileNodes().add(fileInfoExtractor.getFileNode());
            // 设置子节点
            packageNode.setChildren(packageNode.getFileNodes());
//            importCount += fileInfoExtractor.getImportNames().size();
        }
        travelRepoInfo();
    }

    private void travelRepoInfo() {
        for (List<PackageNode> packageInfos : moduleInfos.values()) {
            this.packageInfos.addAll(packageInfos);
            travel(packageInfos);
        }
    }

    @SuppressWarnings("unchecked")
    private void travel(List<? extends  BaseNode> baseNodes) {
        if (baseNodes == null || baseNodes.size() == 0) {
            return;
        }
        switch (baseNodes.get(0).getProjectInfoLevel()) {
            case FILE:
                fileInfos.addAll((List<FileNode>)baseNodes);
                break;
            case CLASS:
                classInfos.addAll((List<ClassNode>)baseNodes);
                for (BaseNode baseNode : baseNodes) {
                    ClassNode c = (ClassNode) baseNode;
                    travel(c.getFieldNodes());
                }
                break;
            case METHOD:
                methodInfos.addAll((List<MethodNode>)baseNodes);
                break;
            case FIELD:
                fieldInfos.addAll((List<FieldNode>)baseNodes);
                break;
            case STATEMENT:
                statementInfos.addAll((List<StatementNode>)baseNodes);
                break;
            default:
                break;
        }
        for (BaseNode baseNode : baseNodes) {
            travel(baseNode.getChildren());
        }
    }


    private PackageNode findPackageInfoByPackageName(PackageNode p1, List<PackageNode> packageInfos) {
        for (PackageNode packageNode : packageInfos) {
            if (p1.getPackageName().equals(packageNode.getPackageName())) {
                return packageNode;
            }
        }
        log.error("could not find package! package name:{},module name:{}", p1.getPackageName(), p1.getModuleName());
        return null;
    }

    private List<String> listJavaFiles(File projectDir) {
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith(".java"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        return pathList;
    }

    /**
     * getter and setter
     * */
    public String getRepoUuid() {
        return repoUuid;
    }

    public String getCommit() {
        return commit;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public JGitHelper getJGitHelper() {
        return jGitHelper;
    }

    public String getBranch() {
        return branch;
    }

//    public int getImportCount() { return importCount; }

//    public void setImportCount(int importCount) { this.importCount = importCount; }

    @org.jetbrains.annotations.Contract(pure = true)
    private String getParentCommit() {
        return this.parentCommit;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public List<PackageNode> getPackageInfos() {
        return packageInfos;
    }

    public void setPackageInfos(List<PackageNode> packageInfos) {
        this.packageInfos = packageInfos;
    }

    public List<FileNode> getFileInfos() {
        return fileInfos;
    }

    public void setFileInfos(List<FileNode> fileInfos) {
        this.fileInfos = fileInfos;
    }

    public List<ClassNode> getClassInfos() {
        return classInfos;
    }

    public void setClassInfos(List<ClassNode> classInfos) {
        this.classInfos = classInfos;
    }

    public List<FieldNode> getFieldInfos() {
        return fieldInfos;
    }

    public void setFieldInfos(List<FieldNode> fieldInfos) {
        this.fieldInfos = fieldInfos;
    }

    public List<MethodNode> getMethodInfos() {
        return methodInfos;
    }

    public void setMethodInfos(List<MethodNode> methodInfos) {
        this.methodInfos = methodInfos;
    }

    public List<StatementNode> getStatementInfos() {
        return statementInfos;
    }

    public void setStatementInfos(List<StatementNode> statementInfos) {
        this.statementInfos = statementInfos;
    }

    public CommonInfo getCommonInfo() {
        return commonInfo;
    }

    public static void main(String[] args) {
        String repoPath = "/Users/tangyuan/Documents/Git/IssueTracker-Master";
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        String branch = "zhonghui20191012";
        RepoInfoBuilder refactor = new RepoInfoBuilder("repoUuid", "ebf4488c79c5b4bfb02ee90fb2f8c235c84f8859",  repoPath, jGitHelper,  branch, "null", null);
        System.out.println("done");
    }
}