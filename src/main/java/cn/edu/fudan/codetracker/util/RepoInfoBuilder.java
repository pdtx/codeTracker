/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 22:26
 **/
package cn.edu.fudan.codetracker.util;


import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
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
    private String commitMessage;
    private String branch;
    private String parentCommit;
    private JGitHelper jGitHelper;
    private BaseInfo baseInfo;

    private Map<String, List<PackageInfo>> moduleInfos;
    private List<PackageInfo> packageInfos;
    private List<FileInfo> fileInfos;
    private List<ClassInfo> classInfos;
    private List<FieldInfo> fieldInfos;
    private List<MethodInfo> methodInfos;
    private List<StatementInfo> statementInfos;

    public RepoInfoBuilder(String repoUuid, String commit, List<String> fileList, JGitHelper jGitHelper, String branch, String parentCommit) {
       constructor(repoUuid, commit, fileList, jGitHelper, branch, parentCommit);
    }

    public RepoInfoBuilder(String repoUuid, String commit, String repoPath, JGitHelper jGitHelper, String branch, String parentCommit) {
        File file = new File(repoPath);
        constructor(repoUuid, commit, listJavaFiles(file), jGitHelper, branch, parentCommit);
    }

    public RepoInfoBuilder(RepoInfoBuilder repoInfo, List<String> filesList, Boolean isFirst) {
        constructor(repoInfo.getRepoUuid(), repoInfo.getCommit(), filesList, repoInfo.getJGitHelper(), repoInfo.getBranch(), isFirst ? repoInfo.getCommit() : repoInfo.getParentCommit());
    }

    private void constructor(String repoUuid, String commit, List<String> fileList, JGitHelper jGitHelper, String branch, String parentCommit){
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
            Date date = FORMATTER.parse(jGitHelper.getCommitTime(commit));
            committer = jGitHelper.getAuthorName(commit);
            commitMessage = jGitHelper.getMess(commit);
            // String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit
            baseInfo = new BaseInfo(repoUuid, branch, commit, date, committer, commitMessage, this.parentCommit);
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
        analyze(fileList);
    }

    private void analyze(List<String> fileList) {
        // 一个module内包含哪些package
        for (String path : fileList) {
            if (path.toLowerCase().contains("test.java")) {
                continue;
            }
            FileInfoExtractor fileInfoExtractor = new FileInfoExtractor(baseInfo, path, repoUuid);
            String packageName = fileInfoExtractor.getPackageName();
            // special situation ： end with .java but empty
            if (packageName == null) {
                continue;
            }
            String moduleName = fileInfoExtractor.getModuleName();
            //BaseInfo baseInfo, List<FileInfo> children, String moduleName, String packageName
            PackageInfo packageInfo = new PackageInfo(baseInfo, moduleName, packageName);
            if (moduleInfos.containsKey(moduleName)) {
                if (moduleInfos.get(moduleName).contains(packageInfo)) {
                    packageInfo = findPackageInfoByPackageName(packageInfo, moduleInfos.get(moduleName));
                } else {
                    moduleInfos.get(moduleName).add(packageInfo);
                }
            } else {
                List<PackageInfo> packageInfos = new ArrayList<>();
                packageInfos.add(packageInfo);
                moduleInfos.put(moduleName, packageInfos);
            }
            fileInfoExtractor.getFileInfo().setPackageUuid(packageInfo.getUuid());
            // 设置父节点
            fileInfoExtractor.getFileInfo().setParent(packageInfo);
            fileInfoExtractor.parseClassInterface();
            packageInfo.getFileInfos().add(fileInfoExtractor.getFileInfo());
            // 设置子节点
            packageInfo.setChildren(packageInfo.getFileInfos());
        }
        travelRepoInfo();
    }
    private void travelRepoInfo() {
        for (List<PackageInfo> packageInfos : moduleInfos.values()) {
            this.packageInfos.addAll(packageInfos);
            travel(packageInfos);
        }
    }

    @SuppressWarnings("unchecked")
    private void travel(List<? extends  BaseInfo> baseInfos) {
        if (baseInfos == null || baseInfos.size() == 0) {
            return;
        }
        switch (baseInfos.get(0).getProjectInfoLevel()) {
            case FILE:
                fileInfos.addAll((List<FileInfo>)baseInfos);
                break;
            case CLASS:
                classInfos.addAll((List<ClassInfo>)baseInfos);
                for (BaseInfo baseInfo : baseInfos) {
                    ClassInfo c = (ClassInfo) baseInfo;
                    travel(c.getMethodInfos());
                    travel(c.getFieldInfos());
                }
                return;
            case METHOD:
                methodInfos.addAll((List<MethodInfo>)baseInfos);
                break;
            case FIELD:
                fieldInfos.addAll((List<FieldInfo>)baseInfos);
                break;
            case STATEMENT:
                statementInfos.addAll((List<StatementInfo>)baseInfos);
                break;
            default:
                    break;
        }
        for (BaseInfo baseInfo : baseInfos) {
            travel(baseInfo.getChildren());
        }
    }


    private PackageInfo findPackageInfoByPackageName(PackageInfo p1, List<PackageInfo> packageInfos) {
        for (PackageInfo packageInfo : packageInfos) {
            if (p1.getPackageName().equals(packageInfo.getPackageName())) {
                return packageInfo;
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

    public List<FileInfo> getFileInfos() {
        return fileInfos;
    }

    public List<PackageInfo> getPackageInfos() {
        return packageInfos;
    }

    public List<ClassInfo> getClassInfos() {
        return classInfos;
    }

    public List<MethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public JGitHelper getJGitHelper() {
        return jGitHelper;
    }

    public String getBranch() {
        return branch;
    }

    @org.jetbrains.annotations.Contract(pure = true)
    private String getParentCommit() {
        return this.parentCommit;
    }

    public static void main(String[] args) {
        String repoPath = "E:\\Lab\\iec-wepm-develop";
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        String branch = "master";
        RepoInfoBuilder refactor = new RepoInfoBuilder("repoUuid", "e7fecbe0fd950e420f46f3aefaa1e315242503b8",  repoPath, jGitHelper,  branch, "null");
        System.out.println("done");
    }

    public List<StatementInfo> getStatementInfos() {
        return statementInfos;
    }
}