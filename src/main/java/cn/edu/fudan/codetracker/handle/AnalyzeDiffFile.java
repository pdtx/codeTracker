/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 16:41
 **/
package cn.edu.fudan.codetracker.handle;


import cn.edu.fudan.codetracker.dao.ClassDao;
import cn.edu.fudan.codetracker.domain.projectInfo.*;
import cn.edu.fudan.codetracker.util.FileInfoExtractor;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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
     * no matter how, file、class、method、field will be always "add"
     * package relation need to be modified
     * */
    void addInfoConstruction(List<String> addFilesList) {
        RepoInfoBuilder addRepoInfo = new RepoInfoBuilder(curRepoInfo, addFilesList, true);

        packageInfoList.addAll(addRepoInfo.getPackageInfos());
        fileInfoList.addAll(addRepoInfo.getFileInfos());
        classInfoList.addAll(addRepoInfo.getClassInfos());
        methodInfoList.addAll(addRepoInfo.getMethodInfos());
        fieldInfoList.addAll(addRepoInfo.getFieldInfos());
    }

    /**
     *   no matter how, file、class、method、field will be always "delete"
     *   package relation need to be modified
     *   for "delete" situation, the field of changeRelation in table of raw_XX is the only one, which needs to be updated
     *   no data needs to be insert
     * */
    void deleteInfoConstruction(List<String> deleteFilesList) {
        RepoInfoBuilder deleteRepoInfo = new RepoInfoBuilder(preRepoInfo, deleteFilesList, false);

        TrackerInfo trackerInfo;
        String relation = RelationShip.DELETE.name();
        for (PackageInfo packageInfo : deleteRepoInfo.getPackageInfos()) {
            trackerInfo = new TrackerInfo(relation);
            packageInfo.setTrackerInfo(trackerInfo);
        }

        for (FileInfo fileInfo : deleteRepoInfo.getFileInfos()) {
            trackerInfo = new TrackerInfo(relation);
            fileInfo.setTrackerInfo(trackerInfo);
        }

        for (ClassInfo classInfo : deleteRepoInfo.getClassInfos()) {
            trackerInfo = new TrackerInfo(relation);
            classInfo.setTrackerInfo(trackerInfo);
        }

        for (MethodInfo methodInfo : deleteRepoInfo.getMethodInfos()) {
            trackerInfo = new TrackerInfo(relation);
            methodInfo.setTrackerInfo(trackerInfo);
        }

        for (FieldInfo fieldInfo : deleteRepoInfo.getFieldInfos()) {
            trackerInfo = new TrackerInfo(relation);
            fieldInfo.setTrackerInfo(trackerInfo);
        }

        // rewrite method hashCode and equals
        packageInfoList.addAll(deleteRepoInfo.getPackageInfos());
        fileInfoList.addAll(deleteRepoInfo.getFileInfos());
        classInfoList.addAll(deleteRepoInfo.getClassInfos());
        methodInfoList.addAll(deleteRepoInfo.getMethodInfos());
        fieldInfoList.addAll(deleteRepoInfo.getFieldInfos());


        //setTrackerInfo(deleteRepoInfo, RelationShip.DELETE);
    }

    /**
     *   no matter how，package、file will always be "change"
     *   according to diffPath
     * */
    void modifyInfoConstruction(RepoInfoBuilder preRepoInfo, List<String> preFileList, RepoInfoBuilder curRepoInfo, List<String> curFileList, List<String> diffPathList) {
        if (diffPathList == null || diffPathList.size() == 0) {
            return;
        }
        String relation = RelationShip.CHANGE.name();
        TrackerInfo trackerInfo;
        for (PackageInfo packageInfo : curRepoInfo.getPackageInfos()) {
            trackerInfo = new TrackerInfo(relation);
            packageInfo.setTrackerInfo(trackerInfo);
        }

        for (FileInfo fileInfo : curRepoInfo.getFileInfos()) {
            trackerInfo = new TrackerInfo(relation);
            fileInfo.setTrackerInfo(trackerInfo);
        }
        packageInfoList.addAll(curRepoInfo.getPackageInfos());
        fileInfoList.addAll(curRepoInfo.getFileInfos());

        /////////////// class /////////////////////////
/*        //add class
        for (ClassInfo curClassInfo : curRepoInfo.getClassInfos()) {
            ClassInfo preClassInfo = findClassInfoByName(curClassInfo, preRepoInfo.getClassInfos());
            if (preClassInfo == null) {
                setTrackerInfo(curClassInfo, RelationShip.ADD.name());
                classInfoList.add(curClassInfo);
                curRepoInfo.getClassInfos().remove(curClassInfo);
                classInfoList.add(curClassInfo);
            }
        }

        // delete class and modify
        for (ClassInfo preClassInfo : preRepoInfo.getClassInfos()) {
            ClassInfo curClassInfo = findClassInfoByName(preClassInfo, curRepoInfo.getClassInfos());
            if (curClassInfo == null) {
                // class 、field、method relation will be
                setTrackerInfo(preClassInfo, RelationShip.DELETE.name());
                preRepoInfo.getClassInfos().remove(preClassInfo);
                classInfoList.add(preClassInfo);
                continue;
            }
            trackerInfo = new TrackerInfo(RelationShip.CHANGE.name());
            curClassInfo.setTrackerInfo(trackerInfo);
        }
        classInfoList.addAll(curRepoInfo.getClassInfos());
        /////////////// class /////////////////////////*/


        for (int i = 0; i < diffPathList.size(); i++) {
            String input ;
            try {
                input = FileUtils.readFileToString(new File(diffPathList.get(i)), "UTF-8");
            }catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            JSONArray diffDetail = JSONArray.parseArray(input);
            String changeRelation;
            String domainType;
            String description;
            String range;
            int begin,end;
            for (int j = 0; j < diffDetail.size(); j++) {
                FileInfoExtractor preFileInfoExtractor = new FileInfoExtractor(preFileList.get(0), null);
                FileInfoExtractor curFileInfoExtractor = new FileInfoExtractor(curFileList.get(0), null);

                JSONObject oneDiff = diffDetail.getJSONObject(j);
                domainType = oneDiff.getString("type1").toLowerCase();
                changeRelation = oneDiff.getString("type2").toLowerCase();
                // Change.Move 是比statement 更细粒度的语句的change
                if ("change.move".equals(changeRelation)) {
                    changeRelation = "change";
                }
                description = oneDiff.getString("description");
                range = oneDiff.getString("range");

                // find class: rename
                // class
                if (domainType.equals("ClassOrInterface".toLowerCase())) {
                    switch (changeRelation){
                        case "insert":
                            handle(curRepoInfo, range, RelationShip.ADD.name());
                            break;
                        case "change":
                            //before change
                            begin = rangeAnalyzeBegin(range.split("-")[0]);
                            end = rangeAnalyzeEnd(range.split("-")[0]);
                            ClassInfo preClassInfo = findClassInfoByRange(preFileInfoExtractor.getClassInfos(), begin, end);
                            // 必须得到root uuid 以及 version 否则无法关联起来
                            TrackerInfo preTrackerInfo = getPreTrackerInfo(preClassInfo);
                            //after change
                            begin = rangeAnalyzeBegin(range.split("-")[1]);
                            end = rangeAnalyzeEnd(range.split("-")[1]);
                            ClassInfo curClassInfo = findClassInfoByRange(curFileInfoExtractor.getClassInfos(), begin, end);
                            if (curClassInfo != null) {
                                curClassInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), preTrackerInfo.getVersion() + 1, preTrackerInfo.getRootUUID()));
                                // 直接入库
                                classInfoList.add(curClassInfo);
                            }
                            break;
                        case "delete":
                            handle(preRepoInfo, range, RelationShip.DELETE.name());
                    }
                    continue;
                }

                // field 暂时不完成
                if ("member".equals(domainType) && !description.toLowerCase().contains("method")) {
                    continue;
                }

                // method change declaration
                if ("member".equals(domainType) && description.toLowerCase().contains("method")) {
                    switch (changeRelation){
                        case "insert":
                            handleMethod(curFileInfoExtractor.getMethodInfos(), range, RelationShip.ADD.name());
                            break;
                        case "change":
                            //before change
                            begin = rangeAnalyzeBegin(range.split("-")[0]);
                            end = rangeAnalyzeEnd(range.split("-")[0]);
                            MethodInfo preMethodInfo = findMethodInfoByRange(preFileInfoExtractor.getMethodInfos(), begin, end);
                            ClassInfo preClassInfo = findClassInfoByRange(preRepoInfo.getClassInfos(), begin, end);
                            // 必须得到root uuid 以及 version 否则无法关联起来
                            TrackerInfo preTrackerInfo = getPreTrackerInfo(preClassInfo);
                            //after change
                            begin = rangeAnalyzeBegin(range.split("-")[1]);
                            end = rangeAnalyzeEnd(range.split("-")[1]);
                            ClassInfo curClassInfo = findClassInfoByRange(curRepoInfo.getClassInfos(), begin, end);
                            if (curClassInfo != null) {
                                curClassInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), preTrackerInfo.getVersion() + 1, preTrackerInfo.getRootUUID()));
                                // 直接入库
                                classInfoList.add(curClassInfo);
                            }
                            break;
                        case "delete":
                            handleMethod(preFileInfoExtractor.getMethodInfos(), range, RelationShip.DELETE.name());
                    }
                }

                // statement
                if ("statement".equals(domainType)) {

                }


                // 查找所有的method 以及处理其关系

            }

        }

    }

    private void handleMethod(List<MethodInfo> methodInfos, String range, String relation) {
        int begin = rangeAnalyzeBegin(range);
        int end = rangeAnalyzeEnd(range);
        MethodInfo methodInfo = findMethodInfoByRange(methodInfos, begin, end);

        if (relation.equals(RelationShip.ADD.name())) {
            methodInfo.setTrackerInfo(new TrackerInfo(relation, 1, methodInfo.getUuid()));
            methodInfoList.add(methodInfo);
        }

        // ??????
        if (relation.equals(RelationShip.DELETE.name())) {
            methodInfo.setTrackerInfo(new TrackerInfo(relation));
            methodInfoList.add(methodInfo);
        }

    }

    private MethodInfo findMethodInfoByRange(List<MethodInfo> methodInfos, int begin, int end) {
        for (MethodInfo methodInfo : methodInfos) {
            if (methodInfo.getBegin() <= begin && methodInfo.getEnd() >= end) {
                return methodInfo;
            }
        }
        return null;
    }

    private TrackerInfo getPreTrackerInfo(ClassInfo preClassInfo) {
        ClassDao classDao = new ClassDao();
        return classDao.getLastedTrackerInfoByFullname(preClassInfo.getFullname());
    }

    private void handle(RepoInfoBuilder repoInfo, String range, String relation) {
        //根据range当前版本的method
        int begin = rangeAnalyzeBegin(range);
        int end = rangeAnalyzeEnd(range);
        ClassInfo classInfo = findClassInfoByRange(repoInfo.getClassInfos(), begin, end);
        if (classInfo != null) {
            setTrackerInfo(classInfo, relation);
            repoInfo.getClassInfos().remove(classInfo);
        }
    }

    private ClassInfo findClassInfoByRange(List<ClassInfo> classInfos, int begin, int end) {
        //
        // 不准 重新写 那么多classInfo 可能有很多个符合情况
        // 应该先找到对应非file
        for (ClassInfo classInfo : classInfos) {
            if (classInfo.getBegin() <= begin && classInfo.getEnd() >= end) {
                return classInfo;
            }
        }
        return null;
    }

    private void setTrackerInfo(ClassInfo classInfo, String relation) {
        final int FIRST_VERSION = 1;
        TrackerInfo trackerInfo;
        trackerInfo = new TrackerInfo(relation);
        classInfo.setTrackerInfo(trackerInfo);

        for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
            trackerInfo = new TrackerInfo(relation);
            methodInfo.setTrackerInfo(trackerInfo);
        }
        for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
            trackerInfo = new TrackerInfo(relation);
            fieldInfo.setTrackerInfo(trackerInfo);
        }

        if (relation.equals(RelationShip.ADD.name())) {
            classInfo.getTrackerInfo().setVersion(FIRST_VERSION);
            classInfo.getTrackerInfo().setRootUUID(classInfo.getUuid());
            for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                methodInfo.getTrackerInfo().setVersion(FIRST_VERSION);
                methodInfo.getTrackerInfo().setRootUUID(classInfo.getUuid());
            }
            for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                fieldInfo.getTrackerInfo().setVersion(FIRST_VERSION);
                fieldInfo.getTrackerInfo().setRootUUID(classInfo.getUuid());
            }
        }

        classInfoList.add(classInfo);
        methodInfoList.addAll(classInfo.getMethodInfos());
        fieldInfoList.addAll(classInfo.getFieldInfos());
    }

    private ClassInfo findClassInfoByName(ClassInfo preClassInfo, List<ClassInfo> classInfos) {
        for (ClassInfo classInfo : classInfos) {
            if (classInfo.getClassName().equals(preClassInfo.getClassName())
                    && classInfo.getPackageName().equals(preClassInfo.getPackageName())
                    && classInfo.getModuleName().equals(preClassInfo.getModuleName())) {
                return classInfo;
            }
        }
        return null;
    }

    private int rangeAnalyzeBegin(String range) {
        // range ：(87,102)
        return Integer.valueOf(range.substring(1,range.length() - 1).split(",")[0]);
    }

    private int rangeAnalyzeEnd(String range) {
        return Integer.valueOf(range.substring(0,range.length() - 1).split(",")[1]);
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