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
        RepoInfoBuilder addRepoInfo = new RepoInfoBuilder(curRepoInfo, addFilesList);
        for (PackageInfo packageInfo : addRepoInfo.getPackageInfos()) {

        }

    }

    void deleteInfoConstruction(List<String> deleteFilesList) {
        RepoInfoBuilder deleteRepoInfo = new RepoInfoBuilder(preRepoInfo, deleteFilesList);

    }

    void modifyInfoConstruction(RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo, List<String> diffPathList) {

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