/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 22:26
 **/
package cn.edu.fudan.codetracker.util;


import cn.edu.fudan.codetracker.domain.RelationShip;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RepoInfoBuilder {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String repoUuid;
    private String commit;
    private String committer;
    private String commitMessage;
    private String branch;
    private String parentCommit;
    private JGitHelper jGitHelper;

    private CommonInfo commonInfo;

    private List<FileInfo> fileInfos;
    private List<ClassInfo> classInfos;
    private List<FieldInfo> fieldInfos;
    private List<MethodInfo> methodInfos;

    private Map<String, List<PackageInfo>> moduleInfos;

    private List<PackageInfo> packageInfos;

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
        fileInfos = new ArrayList<>();
        classInfos = new ArrayList<>();
        fieldInfos = new ArrayList<>();
        methodInfos = new ArrayList<>();
        try{
            Date date = FORMATTER.parse(jGitHelper.getCommitTime(commit));
            committer = jGitHelper.getAuthorName(commit);
            commitMessage = jGitHelper.getMess(commit);
            commonInfo = new CommonInfo(commit, date, commit, date, repoUuid, branch,
                    date, commit, committer, commitMessage, this.parentCommit);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        packageInfos = new ArrayList<>();
        moduleInfos = new HashMap<>();
        analyze(fileList);
    }


    private void analyze(List<String> fileList) {

        // 一个module内包含哪些package
        Map<String, List<String>> modulePackage = new WeakHashMap<>();
        PackageInfo packageInfo;
        for (String path : fileList) {
            if (path.toLowerCase().contains("test.java")) {
                continue;
            }
            FileInfoExtractor fileInfoExtractor = new FileInfoExtractor(path, repoUuid);
            String moduleName = fileInfoExtractor.getModuleName();
            String packageName = fileInfoExtractor.getPackageName();
            // 特殊情况处理 ： 以 .java 结束但是是空文件
            if (packageName == null) {
                continue;
            }
            String packageUuid ;
            // module 出现过
            if (modulePackage.containsKey(moduleName)) {
                if (modulePackage.get(moduleName).contains(packageName)) {
                    packageUuid = findPackageUuidByModuleAndPackage(moduleName, packageName);
                } else {
                    packageUuid = UUID.randomUUID().toString();
                    List<String> packageList = modulePackage.get(moduleName);
                    packageList.add(packageName);

                    packageInfo = new PackageInfo(moduleName, packageName, fileInfoExtractor.getClassInfos());
                    packageInfo.setUuid(packageUuid);
                    packageInfo.setCommonInfo(commonInfo);
                    packageInfos.add(packageInfo);
                }
            } else { // module 没出现过
                packageUuid = UUID.randomUUID().toString();
                List<String> packageList = new ArrayList<>();
                packageList.add(packageName);
                modulePackage.put(moduleName, packageList);

                packageInfo = new PackageInfo(moduleName, packageName, fileInfoExtractor.getClassInfos());
                packageInfo.setUuid(packageUuid);
                packageInfo.setCommonInfo(commonInfo);
                packageInfos.add(packageInfo);
            }


            fileInfoExtractor.getFileInfo().setPackageUuid(packageUuid);
            fileInfoExtractor.parseClassInterface();


            fileInfos.add(fileInfoExtractor.getFileInfo());
            classInfos.addAll(fileInfoExtractor.getClassInfos());
            fieldInfos.addAll(fileInfoExtractor.getFieldInfos());
            methodInfos.addAll(fileInfoExtractor.getMethodInfos());
        }

        boolean isFirst = parentCommit.equals(commit);
        final int firstVersion = 1;
        // 项目第一次分析 设置tracker Info
            if (isFirst) {
                for (PackageInfo packageInfo1 : packageInfos) {
                    packageInfo1.setTrackerInfo(new TrackerInfo(RelationShip.ADD.name(), firstVersion, packageInfo1.getUuid()));
                }
            }
            for (FileInfo fileInfo : fileInfos) {
                fileInfo.setCommonInfo(commonInfo);
                if (isFirst) {
                    fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.ADD.name(), firstVersion, fileInfo.getUuid()));
                }
            }
            for (ClassInfo classInfo : classInfos) {
                classInfo.setCommonInfo(commonInfo);
                if (isFirst) {
                    classInfo.setTrackerInfo(new TrackerInfo(RelationShip.ADD.name(), firstVersion, classInfo.getUuid()));
                }
            }
            for (FieldInfo fieldInfo : fieldInfos) {
                fieldInfo.setCommonInfo(commonInfo);
                if (isFirst) {
                    fieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.ADD.name(), firstVersion, fieldInfo.getUuid()));
                }
            }
            for (MethodInfo methodInfo : methodInfos) {
                methodInfo.setCommonInfo(commonInfo);
                if (isFirst) {
                    methodInfo.setTrackerInfo(new TrackerInfo(RelationShip.ADD.name(), firstVersion,methodInfo.getUuid()));
                }
            }

    }

    private String findPackageUuidByModuleAndPackage(String moduleName, String packageName) {
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo.getPackageName().equals(packageName) && packageInfo.getModuleName().equals(moduleName)) {
                return packageInfo.getUuid();
            }
        }
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
}