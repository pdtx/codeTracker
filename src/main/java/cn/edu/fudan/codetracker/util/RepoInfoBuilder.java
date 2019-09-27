/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 22:26
 **/
package cn.edu.fudan.codetracker.util;


import cn.edu.fudan.codetracker.domain.projectInfo.*;
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
    private String branch;
    private String parentCommit;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    private List<PackageInfo> packageInfosList;
    private List<FileInfo> fileInfos;
    private List<ClassInfo> classInfos;
    private List<MethodInfo> methodInfos;
    private List<FieldInfo> fieldInfos;


    private Map<String, Map<String, PackageInfo>> moduleInfos;
    private Set<PackageInfo> packageInfos;

    // 旧API
    public RepoInfoBuilder(String repoUuid, String commit, String committer, List<String> fileList) {
        this.repoUuid = repoUuid;
        this.commit = commit;
        this.committer = committer;
        packageInfos = new HashSet<>();
        moduleInfos = new HashMap<>();
        analyze(fileList);
    }

    public RepoInfoBuilder(String repoUuid, String commit, String repoPath, JGitHelper jGitHelper, String branch, String parentCommit) {
        this.repoUuid = repoUuid;
        this.commit = commit;
        this.branch = branch;

        Date date;
        try{
            date = FORMATTER.parse(jGitHelper.getCommitTime(commit));
            commonInfo = new CommonInfo(commit, date, commit, date, repoUuid, branch);
            committer = jGitHelper.getAuthorName(commit);
            trackerInfo = new TrackerInfo(date, commit, committer,
                    jGitHelper.getMess(commit), parentCommit);
        }catch (ParseException e) {
            e.printStackTrace();
        }

        packageInfosList = new ArrayList<>();
        packageInfos = new HashSet<>();
        moduleInfos = new HashMap<>();
        File file = new File(repoPath);
        analyze(listJavaFiles(file));
    }


    private void analyze(List<String> fileList) {
        for (String path : fileList) {
            FileInfoExtractor fileInfoExtractor = new FileInfoExtractor(path, repoUuid);
            String moduleName = fileInfoExtractor.getModuleName();
            String packageName = fileInfoExtractor.getPackageName();
            PackageInfo packageInfo ;
            Map<String, PackageInfo> packageInfoMap;
            packageInfoMap = moduleInfos.containsKey(moduleName) ? moduleInfos.get(moduleName) : new HashMap<>();

            if (! packageInfoMap.containsKey(packageName)) {
                if (packageName == null)
                    continue;
                packageInfo = new PackageInfo(moduleName, packageName, fileInfoExtractor.getClassInfos());
                packageInfoMap.put(packageName, packageInfo);
            } else {
                packageInfo = packageInfoMap.get(packageName);
                for (ClassInfo classInfo : fileInfoExtractor.getClassInfos()) {
                    packageInfo.getClassInfos().add(classInfo);
                }
            }
            moduleInfos.put(moduleName, packageInfoMap);
        }
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
        return packageInfosList;
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