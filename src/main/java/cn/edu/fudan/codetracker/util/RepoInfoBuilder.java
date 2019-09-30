/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 22:26
 **/
package cn.edu.fudan.codetracker.util;


import cn.edu.fudan.codetracker.domain.projectInfo.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;

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

    private CommonInfo commonInfo;

    private List<FileInfo> fileInfos;
    private List<ClassInfo> classInfos;
    private List<FieldInfo> fieldInfos;
    private List<MethodInfo> methodInfos;



    private Map<String, Map<String, PackageInfo>> moduleInfos;
    private List<PackageInfo> packageInfos;

    // 旧API
    public RepoInfoBuilder(String repoUuid, String commit, String committer, List<String> fileList) {
        this.repoUuid = repoUuid;
        this.commit = commit;
        this.committer = committer;
        packageInfos = new ArrayList<>();
        moduleInfos = new HashMap<>();
        analyze(fileList);
    }

    public RepoInfoBuilder(String repoUuid, String commit, String repoPath, JGitHelper jGitHelper, String branch, String parentCommit) {
        this.repoUuid = repoUuid;
        this.commit = commit;
        this.branch = branch;
        fileInfos = new ArrayList<>();
        classInfos = new ArrayList<>();
        fieldInfos = new ArrayList<>();
        methodInfos = new ArrayList<>();

        try{
            Date date = FORMATTER.parse(jGitHelper.getCommitTime(commit));

            committer = jGitHelper.getAuthorName(commit);
            commitMessage = jGitHelper.getMess(commit);
            commonInfo = new CommonInfo(commit, date, commit, date, repoUuid, branch,
                    date, commit, committer, commitMessage, parentCommit);
        }catch (ParseException e) {
            e.printStackTrace();
        }

        packageInfos = new ArrayList<>();
        moduleInfos = new HashMap<>();
        File file = new File(repoPath);
        analyze(listJavaFiles(file));
    }


    private void analyze(List<String> fileList) {

        // 一个module内包含哪些package
        Map<String, List<String>> modulePackage = new WeakHashMap<>();
        PackageInfo packageInfo;
        for (String path : fileList) {
            FileInfoExtractor fileInfoExtractor = new FileInfoExtractor(path, repoUuid);
            String moduleName = fileInfoExtractor.getModuleName();
            String packageName = fileInfoExtractor.getPackageName();
            // 特殊情况处理 ： 以 .java 处理 但是是空文件
            if (packageName == null)
                continue;
            String packageUUID ;
            // module 出现过
            if (modulePackage.containsKey(moduleName)) {
                if (modulePackage.get(moduleName).contains(packageName)) {
                    packageUUID = findPackageUUIDbyModuleAndPackage(moduleName, packageName);
                } else {
                    packageUUID = UUID.randomUUID().toString();
                    List<String> packageList = modulePackage.get(moduleName);
                    packageList.add(packageName);

                    packageInfo = new PackageInfo(moduleName, packageName, fileInfoExtractor.getClassInfos());
                    packageInfo.setUuid(packageUUID);
                    packageInfo.setCommonInfo(commonInfo);
                    packageInfos.add(packageInfo);
                }
            } else { // module 没出现过
                packageUUID = UUID.randomUUID().toString();
                List<String> packageList = new ArrayList<>();
                packageList.add(packageName);
                modulePackage.put(moduleName, packageList);

                packageInfo = new PackageInfo(moduleName, packageName, fileInfoExtractor.getClassInfos());
                packageInfo.setUuid(packageUUID);
                packageInfo.setCommonInfo(commonInfo);
                packageInfos.add(packageInfo);
            }


            fileInfoExtractor.getFileInfo().setpackageUuid(packageUUID);
            fileInfoExtractor.parseClassInterface();


            fileInfos.add(fileInfoExtractor.getFileInfo());
            classInfos.addAll(fileInfoExtractor.getClassInfos());
            fieldInfos.addAll(fileInfoExtractor.getFieldInfos());
            methodInfos.addAll(fileInfoExtractor.getMethodInfos());
        }

        // 项目第一次分析 设置common Info
        if (parentCommit == null) {
            for (PackageInfo packageInfo1 : packageInfos) {
                packageInfo1.setTrackerInfo("ADD",1,packageInfo1.getUuid());
            }
            for (FileInfo fileInfo : fileInfos) {
                fileInfo.setCommonInfo(commonInfo);
                fileInfo.setTrackerInfo("ADD",1,fileInfo.getUuid());
            }
            for (ClassInfo classInfo : classInfos) {
                classInfo.setCommonInfo(commonInfo);
                classInfo.setTrackerInfo("ADD",1,classInfo.getUuid());
            }
            for (FieldInfo fieldInfo : fieldInfos) {
                fieldInfo.setCommonInfo(commonInfo);
                fieldInfo.setTrackerInfo("ADD",1,fieldInfo.getUuid());
            }
            for (MethodInfo methodInfo : methodInfos) {
                methodInfo.setCommonInfo(commonInfo);
                methodInfo.setTrackerInfo("ADD",1,methodInfo.getUuid());
            }
        }
    }

    private String findPackageUUIDbyModuleAndPackage(String moduleName, String packageName) {
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo.getPackageName().equals(packageName) && packageInfo.getModuleName().equals(moduleName))
                return packageInfo.getUuid();
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
    public String getrepoUuid() {
        return repoUuid;
    }

    public String getCommit() {
        return commit;
    }

    public String getCommitter() {
        return committer;
    }

    public Map<String, PackageInfo> getPackageInfosByModuleName(String moduleName) {
        return moduleInfos.get(moduleName);
    }

    public Map<String, Map<String, PackageInfo>> getModuleInfos() {
        return moduleInfos;
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
}