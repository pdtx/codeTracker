/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 16:41
 **/
package cn.edu.fudan.codetracker.handle;


import cn.edu.fudan.codetracker.domain.projectInfo.*;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;

import java.util.*;

public class AnalyzeDiffFile {

    private RepoInfoBuilder preRepoInfo;
    private RepoInfoBuilder curRepoInfo;
    private List<PackageInfo> packageInfoList;
    private List<FileInfo> fileInfoList;
    private List<ClassInfo> classInfoList;
    private List<MethodInfo> methodInfoList;
    private List<FieldInfo> fieldInfoList;


    AnalyzeDiffFile(RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo) {
        this.preRepoInfo = preRepoInfo;
        this.curRepoInfo = curRepoInfo;
        packageInfoList = new ArrayList<>();
        fileInfoList = new ArrayList<>();
        classInfoList = new ArrayList<>();
        methodInfoList = new ArrayList<>();
        fieldInfoList = new ArrayList<>();
    }


    /**
     * 主要需要设置tracker Info
     * 主要解析relation信息
     * version and rootUUID get from dbTable
     * */
    void addInfoConstruction(List<String> addFilesList) {
        RepoInfoBuilder addRepoInfo = new RepoInfoBuilder(curRepoInfo, addFilesList, true);

        // no matter how, file、class、method、field will be always "add"
        // package relation need to be modified

        packageInfoList.addAll(addRepoInfo.getPackageInfos());
        fileInfoList.addAll(addRepoInfo.getFileInfos());
        classInfoList.addAll(addRepoInfo.getClassInfos());
        methodInfoList.addAll(addRepoInfo.getMethodInfos());
        fieldInfoList.addAll(addRepoInfo.getFieldInfos());
    }

    /**
     *   no matter how, file、class、method、field will be always "delete"
     *   package relation need to be modified
     *   for "delete" situation, the changeRelation in table of raw_XX is the only field, which needs to be updated
     *   no data needs to be insert
     * */
    void deleteInfoConstruction(List<String> deleteFilesList) {
        RepoInfoBuilder deleteRepoInfo = new RepoInfoBuilder(preRepoInfo, deleteFilesList, false);

        TrackerInfo trackerInfo;
        String relation = RelationShip.DELETE.name();
        for (PackageInfo packageInfo : preRepoInfo.getPackageInfos()) {
            trackerInfo = new TrackerInfo(relation);
            packageInfo.setTrackerInfo(trackerInfo);
        }

        for (FileInfo fileInfo : preRepoInfo.getFileInfos()) {
            trackerInfo = new TrackerInfo(relation);
            fileInfo.setTrackerInfo(trackerInfo);
        }

        for (ClassInfo classInfo : preRepoInfo.getClassInfos()) {
            trackerInfo = new TrackerInfo(relation);
            classInfo.setTrackerInfo(trackerInfo);
        }

        for (MethodInfo methodInfo : preRepoInfo.getMethodInfos()) {
            trackerInfo = new TrackerInfo(relation);
            methodInfo.setTrackerInfo(trackerInfo);
        }

        for (FieldInfo fieldInfo : preRepoInfo.getFieldInfos()) {
            trackerInfo = new TrackerInfo(relation);
            fieldInfo.setTrackerInfo(trackerInfo);
        }

        // need to rewrite method hashCode and equals
        packageInfoList.addAll(preRepoInfo.getPackageInfos());
        fileInfoList.addAll(preRepoInfo.getFileInfos());
        classInfoList.addAll(preRepoInfo.getClassInfos());
        methodInfoList.addAll(preRepoInfo.getMethodInfos());
        fieldInfoList.addAll(preRepoInfo.getFieldInfos());



        //setTrackerInfo(deleteRepoInfo, RelationShip.DELETE);
    }

    void modifyInfoConstruction(RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo, List<String> diffPathList) {
        if (diffPathList == null || diffPathList.size() == 0) {
            return;
        }

        // 不管怎么样 package、file、class 都是change
        // 根据diff path 来构造relation


    }




    
    private void setTrackerInfo(RepoInfoBuilder repoInfo,RelationShip changeRelation) {
        TrackerInfo trackerInfo;

        for (PackageInfo packageInfo : repoInfo.getPackageInfos()) {
            trackerInfo = new TrackerInfo(changeRelation.name());
            packageInfo.setTrackerInfo(trackerInfo);
        }

        for (FileInfo fileInfo : repoInfo.getFileInfos()) {
            trackerInfo = new TrackerInfo(changeRelation.name());
            fileInfo.setTrackerInfo(trackerInfo);
        }

        for (ClassInfo classInfo : repoInfo.getClassInfos()) {
            trackerInfo = new TrackerInfo(changeRelation.name());
            classInfo.setTrackerInfo(trackerInfo);
        }

        for (MethodInfo methodInfo : repoInfo.getMethodInfos()) {
            trackerInfo = new TrackerInfo(changeRelation.name());
            methodInfo.setTrackerInfo(trackerInfo);
        }

        for (FieldInfo fieldInfo : repoInfo.getFieldInfos()) {
            trackerInfo = new TrackerInfo(changeRelation.name());
            fieldInfo.setTrackerInfo(trackerInfo);
        }

        // need to rewrite method hashCode and equals
        packageInfoList.addAll(repoInfo.getPackageInfos());
        fileInfoList.addAll(repoInfo.getFileInfos());
        classInfoList.addAll(repoInfo.getClassInfos());
        methodInfoList.addAll(repoInfo.getMethodInfos());
        fieldInfoList.addAll(repoInfo.getFieldInfos());
    }

    /**
     * getter and setter
     * */
    public List<PackageInfo> getPackageInfoList() {
        return packageInfoList;
    }

    public List<FileInfo> getFileInfoList() {
        return fileInfoList;
    }

    public List<ClassInfo> getClassInfoList() {
        return classInfoList;
    }

    public List<MethodInfo> getMethodInfoList() {
        return methodInfoList;
    }

    public List<FieldInfo> getFieldInfoList() {
        return fieldInfoList;
    }
}