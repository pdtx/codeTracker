/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 16:41
 **/
package cn.edu.fudan.codetracker.handle;


import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AnalyzeDiffFile {

    private PackageDao packageDao;
    private FileDao fileDao;
    private ClassDao classDao;
    private FieldDao fieldDao;
    private MethodDao methodDao;

    //private RepoInfoBuilder preRepoInfo;
    private RepoInfoBuilder curRepoInfo;

    private Map<String, Set<PackageInfo>> packageInfos;
    private Map<String, Set<FileInfo>> fileInfos;
    private Map<String, Set<ClassInfo>> classInfos;
    private Map<String, Set<MethodInfo>> methodInfos;
    private Map<String, Set<FieldInfo>> fieldInfos;

    // hash，value 修改后
    private Map<Integer, TrackerInfo> modifyPackageUUID;
/*
    private Map<Integer, TrackerInfo> changeClass;
    private Map<Integer, TrackerInfo> changeMethod;
*/


/*    private List<PackageInfo> packageInfoList;
    private List<FileInfo> fileInfoList;
    private List<ClassInfo> classInfoList;
    private List<MethodInfo> methodInfoList;
    private List<FieldInfo> fieldInfoList;*/

    AnalyzeDiffFile(PackageDao packageDao, FileDao fileDao, ClassDao classDao, FieldDao fieldDao, MethodDao methodDao, RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo) {
        this.packageDao = packageDao;
        this.fileDao = fileDao;
        this.classDao = classDao;
        this.fieldDao = fieldDao;
        this.methodDao = methodDao;
        //this.preRepoInfo = preRepoInfo;
        this.curRepoInfo = curRepoInfo;

        packageInfos = new HashMap<>();
        fileInfos = new HashMap<>();
        classInfos = new HashMap<>();
        methodInfos = new HashMap<>();
        fieldInfos = new HashMap<>();
        init(packageInfos);
        init(fileInfos);
        init(classInfos);
        init(methodInfos);
        init(fieldInfos);
        modifyPackageUUID = new HashMap<>();
    }

/*    private void initConstructor() {
        packageInfos = new HashMap<>();
        fileInfos = new HashMap<>();
        classInfos = new HashMap<>();
        methodInfos = new HashMap<>();
        fieldInfos = new HashMap<>();
        init(packageInfos);
        init(fileInfos);
        init(classInfos);
        init(methodInfos);
        init(fieldInfos);

        // 通过new的实例 mapper 注入不了
*//*        packageDao = new PackageDao();
        fileDao = new FileDao();
        classDao = new ClassDao();
        fieldDao = new FieldDao();
        methodDao = new MethodDao();*//*

        // key 修改前，value 修改后
        modifyPackageUUID = new HashMap<>();
*//*        packageInfoList = new ArrayList<>();
        fileInfoList = new ArrayList<>();
        classInfoList = new ArrayList<>();
        methodInfoList = new ArrayList<>();
        fieldInfoList = new ArrayList<>();*//*
    }*/

    private <T> void init(Map<String, Set<T>> map) {
        Set<T> add = new HashSet<>();
        Set<T> delete = new HashSet<>();
        Set<T> change = new HashSet<>();
        map.put(RelationShip.ADD.name(), add);
        map.put(RelationShip.DELETE.name(), delete);
        map.put(RelationShip.CHANGE.name(), change);
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

        // 更新packageUUID
        for (PackageInfo packageInfo : addRepoInfo.getPackageInfos()) {
            TrackerInfo trackerInfo = packageDao.getTrackerInfo(packageInfo.getModuleName(), packageInfo.getPackageName());
            if (trackerInfo != null) {
                modifyPackageUUID.put(packageInfo.hashCode(), trackerInfo);
                packageInfo.setTrackerInfo(new TrackerInfo( RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1,trackerInfo.getRootUUID()));
                packageInfos.get(RelationShip.CHANGE.name()).add(packageInfo);
            } else {
                packageInfos.get(RelationShip.ADD.name()).add(packageInfo);
            }
        }
        fileInfos.get(RelationShip.ADD.name()).addAll(addRepoInfo.getFileInfos());
        classInfos.get(RelationShip.ADD.name()).addAll(addRepoInfo.getClassInfos());
        methodInfos.get(RelationShip.ADD.name()).addAll(addRepoInfo.getMethodInfos());
        fieldInfos.get(RelationShip.ADD.name()).addAll(addRepoInfo.getFieldInfos());
/*        for (FileInfo fileInfo : addRepoInfo.getFileInfos()) {
            if (modifyPackageUUID.containsKey(fileInfo.getPackageUuid())) {
                fileInfo.setPackageUuid(modifyPackageUUID.get(fileInfo.getPackageUuid()));
            }
            fileInfos.get(RelationShip.ADD.name()).add(fileInfo);
        }

        for (ClassInfo classInfo : addRepoInfo.getClassInfos()) {
            if (modifyPackageUUID.containsKey(classInfo.getPackageUuid())) {
                classInfo.setPackageUuid(modifyPackageUUID.get(classInfo.getPackageUuid()));
            }
            classInfos.get(RelationShip.ADD.name()).add(classInfo);
        }

        for (MethodInfo methodInfo : addRepoInfo.getMethodInfos()) {
            if (modifyPackageUUID.containsKey(methodInfo.getPackageUuid())) {
                methodInfo.setPackageUuid(modifyPackageUUID.get(methodInfo.getPackageUuid()));
            }
            methodInfos.get(RelationShip.ADD.name()).add(methodInfo);
        }

        for (FieldInfo fieldInfo : addRepoInfo.getFieldInfos()) {
            if (modifyPackageUUID.containsKey(fieldInfo.getPackageUuid())) {
                fieldInfo.setPackageUuid(modifyPackageUUID.get(fieldInfo.getPackageUuid()));
            }
            fieldInfos.get(RelationShip.ADD.name()).add(fieldInfo);
        }*/
    }

    /**
     *   no matter how, file、class、method、field will be always "delete"
     *   package relation need to be modified
     *   for "delete" situation, the field of changeRelation in table of raw_XX is the only one, which needs to be updated
     *   no data needs to be insert
     * */
    void deleteInfoConstruction(List<String> deleteFilesList) {
        // 删除的数据也作为一条插入
        // 基本数据和删除的前一样 relation 为delete表示在这个版本删除 删除人是谁 相关的删除信息等
        // 用curRepoInfo 来初始化 表示在current 这个版本删除
        RepoInfoBuilder deleteRepoInfo = new RepoInfoBuilder(curRepoInfo, deleteFilesList, false);

        TrackerInfo trackerInfo;
        String relation = RelationShip.DELETE.name();
        for (PackageInfo packageInfo : deleteRepoInfo.getPackageInfos()) {
            // 判断这个package是不是delete 需要判断这个package下是否还有file
            if (isPackageDelete(packageInfo)) {
                packageInfos.get(RelationShip.DELETE.name()).add(packageInfo);
            } else {
                int hashCode = packageInfo.hashCode();
                if (! modifyPackageUUID.containsKey(hashCode)) {
                    trackerInfo = packageDao.getTrackerInfo(packageInfo.getModuleName(), packageInfo.getPackageName());
                    modifyPackageUUID.put(hashCode, trackerInfo);
                    packageInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name() , trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                    packageInfos.get(RelationShip.CHANGE.name()).add(packageInfo);
                }
            }
        }

        for (FileInfo fileInfo : deleteRepoInfo.getFileInfos()) {
            trackerInfo = fileDao.getTrackerInfo(fileInfo.getFilePath());
            trackerInfo.setChangeRelation(relation);
            fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            fileInfos.get(RelationShip.DELETE.name()).add(fileInfo);
        }

        for (ClassInfo classInfo : deleteRepoInfo.getClassInfos()) {
            trackerInfo = classDao.getTrackerInfo(classInfo.getFilePath(), classInfo.getClassName());
            trackerInfo.setChangeRelation(relation);
            classInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            classInfos.get(RelationShip.DELETE.name()).add(classInfo);
        }

        for (MethodInfo methodInfo : deleteRepoInfo.getMethodInfos()) {
            trackerInfo = methodDao.getTrackerInfo(methodInfo.getFilePath(), methodInfo.getClassName(), methodInfo.getSignature());
            trackerInfo.setChangeRelation(relation);
            methodInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            methodInfos.get(RelationShip.DELETE.name()).add(methodInfo);
        }

        for (FieldInfo fieldInfo : deleteRepoInfo.getFieldInfos()) {
            trackerInfo = fieldDao.getTrackerInfo(fieldInfo.getFilePath(), fieldInfo.getClassName(), fieldInfo.getSimpleName());
            trackerInfo.setChangeRelation(relation);
            fieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            fieldInfos.get(RelationShip.DELETE.name()).add(fieldInfo);
        }

        //setTrackerInfo(deleteRepoInfo, RelationShip.DELETE);
    }

    /**
     *      重写
     * */
    private boolean isPackageDelete(PackageInfo packageInfo) {
        return false;
    }

    /**
     *   no matter how，package、file will always be "change"
     *   according to diffPath
     * */
    void modifyInfoConstruction(RepoInfoBuilder preRepoInfo, List<String> fileNameList, RepoInfoBuilder curRepoInfo, List<String> diffPathList) {
        if (diffPathList == null || diffPathList.size() == 0) {
            return;
        }
        String relation = RelationShip.CHANGE.name();
        TrackerInfo trackerInfo;
        for (PackageInfo packageInfo : curRepoInfo.getPackageInfos()) {
            int hashCode = packageInfo.hashCode();
            if (! modifyPackageUUID.containsKey(hashCode)) {
                trackerInfo = packageDao.getTrackerInfo(packageInfo.getModuleName(), packageInfo.getPackageName());
                modifyPackageUUID.put(hashCode, trackerInfo);
                packageInfo.setTrackerInfo(new TrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                packageInfos.get(relation).add(packageInfo);
            }
        }

        for (FileInfo fileInfo : curRepoInfo.getFileInfos()) {
            trackerInfo = fileDao.getTrackerInfo(fileInfo.getFilePath());
            fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name() , trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
            fileInfos.get(relation).add(fileInfo);
        }



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

        Set<String> uuidList = new HashSet<>();
        boolean isFirst = true;
        for (int i = 0; i < diffPathList.size(); i++) {
            String input ;
            try {
                input = FileUtils.readFileToString(new File(diffPathList.get(i)), "UTF-8");
            }catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            String filePath = fileNameList.get(i);
            JSONArray diffDetail = JSONArray.parseArray(input);
            String changeRelation;
            String domainType;
            String description;
            String range;
            int begin,end;


            // field
            // add
            if (isFirst) {
                for (FieldInfo curFieldInfo : curRepoInfo.getFieldInfos()) {
                    FieldInfo preFieldInfo = findFieldInfoByName(curFieldInfo.getSimpleName(), preRepoInfo.getFieldInfos(), filePath);
                    if (preFieldInfo == null) {
                        curFieldInfo.setTrackerInfo(RelationShip.ADD.name(), 1, curFieldInfo.getUuid());
                        fieldInfos.get(RelationShip.ADD.name()).add(curFieldInfo);
                        // 减少后续查找时间
                        //curRepoInfo.getFieldInfos().remove(curFieldInfo);
                        uuidList.add(curFieldInfo.getUuid());
                    }
                }

                for (FieldInfo preFieldInfo : preRepoInfo.getFieldInfos()) {
                    FieldInfo curFieldInfo = findFieldInfoByName(preFieldInfo.getSimpleName(), curRepoInfo.getFieldInfos(), filePath);
                    trackerInfo = fieldDao.getTrackerInfo(preFieldInfo.getFilePath(), preFieldInfo.getClassName(), preFieldInfo.getSimpleName());
                    if (curFieldInfo == null) {
                        preFieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                        fieldInfos.get(RelationShip.DELETE.name()).add(preFieldInfo);
                        //preRepoInfo.getFieldInfos().remove(preFieldInfo);
                    } else if (!uuidList.contains(curFieldInfo.getUuid())) {
                        curFieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                        fieldInfos.get(RelationShip.CHANGE.name()).add(curFieldInfo);
                        //curRepoInfo.getFieldInfos().remove(curFieldInfo);
                    }
                }
                isFirst = false;
            }

            for (int j = 0; j < diffDetail.size(); j++) {
                JSONObject oneDiff = diffDetail.getJSONObject(j);
                domainType = oneDiff.getString("type1").toLowerCase();
                changeRelation = oneDiff.getString("type2").toLowerCase();
                // Change.Move 是比statement 更细粒度的语句的change
                if ("change.move".equals(changeRelation)) {
                    changeRelation = "change";
                }
                description = oneDiff.getString("description");
                range = oneDiff.getString("range");

                ////////////////////////////////// class //////////////////
                // find class: rename
                if (domainType.equals("ClassOrInterface".toLowerCase())) {
                    switch (changeRelation){
                        case "insert":
                            handleClass(curRepoInfo, filePath, range, RelationShip.ADD.name());
                            break;
                        case "change":
                            //before change
                            begin = rangeAnalyzeBegin(range.split("-")[0]);
                            end = rangeAnalyzeEnd(range.split("-")[0]);
                            ClassInfo preClassInfo = findClassInfoByRange(preRepoInfo.getClassInfos(), filePath, begin, end);
                            // 必须得到root uuid 以及 version 否则无法关联起来
                            TrackerInfo preTrackerInfo = classDao.getTrackerInfo(preClassInfo.getFilePath(), preClassInfo.getClassName());
                            //after change
                            begin = rangeAnalyzeBegin(range.split("-")[1]);
                            end = rangeAnalyzeEnd(range.split("-")[1]);
                            ClassInfo curClassInfo = findClassInfoByRange(curRepoInfo.getClassInfos(), filePath, begin, end);
                            if (curClassInfo != null) {
                                curClassInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), preTrackerInfo.getVersion() + 1, preTrackerInfo.getRootUUID()));
                                // 直接入库
                                classInfos.get(RelationShip.CHANGE.name()).add(curClassInfo);
                                //curRepoInfo.getClassInfos().remove(curClassInfo);
                            }
                            break;
                        case "delete":
                            handleClass(preRepoInfo, filePath, range, RelationShip.DELETE.name());
                    }
                    continue;
                }



                // method change declaration
                if ("member".equals(domainType) && description.toLowerCase().contains("method")) {
                    switch (changeRelation){
                        case "insert":
                            handleMethod(curRepoInfo.getMethodInfos(), filePath, range, RelationShip.ADD.name());
                            break;
                        case "change":
                            //before change
                            begin = rangeAnalyzeBegin(range.split("-")[0]);
                            end = rangeAnalyzeEnd(range.split("-")[0]);
                            MethodInfo preMethodInfo = findMethodInfoByRange(preRepoInfo.getMethodInfos(), filePath, begin, end);
                            if (preMethodInfo == null) {
                                System.out.println("===========preMethodInfo===============");
                                System.out.println(range);
                                System.out.println("============preMethodInfo==============");
                                break;
                            }
                            try {
                                // 必须得到root uuid 以及 version 否则无法关联起来
                                trackerInfo = methodDao.getTrackerInfo(preMethodInfo.getFilePath(), preMethodInfo.getClassName(), preMethodInfo.getSignature());
                                //after change
                                begin = rangeAnalyzeBegin(range.split("-")[1]);
                                end = rangeAnalyzeEnd(range.split("-")[1]);
                                MethodInfo curMethodInfo = findMethodInfoByRange(curRepoInfo.getMethodInfos(), filePath, begin, end);
                                if (curMethodInfo != null) {
                                    curMethodInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                                    // 直接入库
                                    methodInfos.get(RelationShip.CHANGE.name()).add(curMethodInfo);
                                }
                            }catch (NullPointerException e) {
                                e.printStackTrace();
                                System.out.println("==========================");
                                System.out.println(range);
                                System.out.println("==========================");
                            }

                            break;
                        case "delete":
                            handleMethod(preRepoInfo.getMethodInfos(), filePath, range, RelationShip.DELETE.name());
                    }
                }

                // statement
                MethodInfo curMethodInfo = null;
                if ("statement".equals(domainType)) {
                    switch (changeRelation){
                        case "insert":
                            begin = rangeAnalyzeBegin(range);
                            end = rangeAnalyzeEnd(range);
                            curMethodInfo = findMethodInfoByRange(curRepoInfo.getMethodInfos(), filePath, begin, end);
                            break;
                        case "delete":
                            begin = rangeAnalyzeBegin(range);
                            end = rangeAnalyzeEnd(range);
                            MethodInfo preMethodInfo = findMethodInfoByRange(preRepoInfo.getMethodInfos(), filePath, begin, end);
                            for (MethodInfo methodInfo : curRepoInfo.getMethodInfos()) {
                                if (methodInfo.getFullname().equals(preMethodInfo.getFullname())) {
                                    curMethodInfo = methodInfo;
                                    break;
                                }
                            }
                            break;
                        case "change":
                            try {
                                begin = rangeAnalyzeBegin(range.split("-")[1]);
                                end = rangeAnalyzeEnd(range.split("-")[1]);
                                // 根据begin 与 end 找到 method
                                curMethodInfo = findMethodInfoByRange(curRepoInfo.getMethodInfos(), filePath, begin, end);
                            }catch (ArrayIndexOutOfBoundsException e) {
                                System.out.println(diffPathList.get(i));
                            }
                    }
                    if (curMethodInfo != null && !methodInfos.get(RelationShip.CHANGE.name()).contains(curMethodInfo)) {
                        trackerInfo = methodDao.getTrackerInfo(curMethodInfo.getFilePath(), curMethodInfo.getClassName(), curMethodInfo.getSignature());
                        curMethodInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                        methodInfos.get(RelationShip.CHANGE.name()).add(curMethodInfo);
                        //curRepoInfo.getMethodInfos().remove(curMethodInfo);// 减少下次搜索时间
                    }
                }

            }

        }

    }

    private FieldInfo findFieldInfoByName(String fieldName, List<FieldInfo> fieldInfos, String filePath) {
        for (FieldInfo fieldInfo : fieldInfos) {
            if (filePath.equals(fieldInfo.getFilePath()) && fieldName.equals(fieldInfo.getSimpleName())) {
                return fieldInfo;
            }
        }
        return null;
    }


    private void handleMethod(List<MethodInfo> methodInfoList, String filePath, String range, String relation) {
        int begin = rangeAnalyzeBegin(range);
        int end = rangeAnalyzeEnd(range);
        MethodInfo methodInfo = findMethodInfoByRange(methodInfoList, filePath, begin, end);
        if (methodInfo == null) {
            return;
        }

        // add
        if (relation.equals(RelationShip.ADD.name())) {
            methodInfo.setTrackerInfo(new TrackerInfo(relation, 1, methodInfo.getUuid()));
            methodInfos.get(RelationShip.ADD.name()).add(methodInfo);
        }

        // delete: 只有changeRelation 改变
        if (relation.equals(RelationShip.DELETE.name())) {
            TrackerInfo trackerInfo = methodDao.getTrackerInfo(methodInfo.getFilePath(), methodInfo.getClassName(), methodInfo.getSignature());
            methodInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            methodInfos.get(RelationShip.DELETE.name()).add(methodInfo);
        }

    }

    private MethodInfo findMethodInfoByRange( List<MethodInfo> methodInfos, String filePath,int begin, int end) {
        for (MethodInfo methodInfo : methodInfos) {
            if (filePath.equals(methodInfo.getFilePath()) && methodInfo.getBegin() <= begin && methodInfo.getEnd() >= end) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * fileInfoExtractor 查找的更准确，但是package信息不准确
     *
     * */
    private void handleClass(RepoInfoBuilder repoInfo, String path, String range, String relation) {
        //根据range当前版本的method
        int begin = rangeAnalyzeBegin(range);
        int end = rangeAnalyzeEnd(range);
        ClassInfo classInfo = findClassInfoByRange(repoInfo.getClassInfos(), path, begin, end);
        if (classInfo != null) {
            setTrackerInfo(classInfo, relation);
            //repoInfo.getClassInfos().remove(classInfo);
        }
    }

    private ClassInfo findClassInfoByRange(List<ClassInfo> classInfos, String path, int begin, int end) {
        // 应该先找到对应非file
        for (ClassInfo classInfo : classInfos) {
            if (path.equals(classInfo.getFilePath()) && classInfo.getBegin() <= begin && classInfo.getEnd() >= end) {
                return classInfo;
            }
        }
        return null;
    }

    private void setTrackerInfo(ClassInfo classInfo, String relation) {
        final int FIRST_VERSION = 1;

        if (relation.equals(RelationShip.ADD.name())) {
            classInfo.setTrackerInfo(new TrackerInfo(relation, FIRST_VERSION, classInfo.getUuid()));
            classInfos.get(relation).add(classInfo);
            for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                methodInfo.setTrackerInfo(new TrackerInfo(relation, FIRST_VERSION, classInfo.getUuid()));
                methodInfos.get(relation).add(methodInfo);
            }
            for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                fieldInfo.setTrackerInfo(new TrackerInfo(relation, FIRST_VERSION, classInfo.getUuid()));
                fieldInfos.get(relation).add(fieldInfo);
            }
        } else {
            TrackerInfo trackerInfo = classDao.getTrackerInfo(classInfo.getFilePath(), classInfo.getClassName());
            classInfo.setTrackerInfo(new TrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            classInfos.get(relation).add(classInfo);

            for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                trackerInfo = methodDao.getTrackerInfo(methodInfo.getFilePath(), methodInfo.getClassName(), methodInfo.getSignature());
                methodInfo.setTrackerInfo(new TrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                methodInfos.get(relation).add(methodInfo);
            }

            for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                trackerInfo = fieldDao.getTrackerInfo(fieldInfo.getFilePath(), fieldInfo.getClassName(), fieldInfo.getSimpleName());
                fieldInfo.setTrackerInfo(new TrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                fieldInfos.get(relation).add(fieldInfo);
            }
        }
    }

/*    private ClassInfo findClassInfoByName(ClassInfo preClassInfo, List<ClassInfo> classInfos) {
        for (ClassInfo classInfo : classInfos) {
            if (classInfo.getClassName().equals(preClassInfo.getClassName())
                    && classInfo.getPackageName().equals(preClassInfo.getPackageName())
                    && classInfo.getModuleName().equals(preClassInfo.getModuleName())) {
                return classInfo;
            }
        }
        return null;
    }*/

    private int rangeAnalyzeBegin(String range) {
        // range ：(87,102)
        return Integer.valueOf(range.substring(1,range.length() - 1).split(",")[0]);
    }

    private int rangeAnalyzeEnd(String range) {
        return Integer.valueOf(range.substring(0,range.length() - 1).split(",")[1]);
    }

    /**
     * getter and setter
     * */
/*    @Autowired
    public void setPackageDao(PackageDao packageDao) {
        this.packageDao = packageDao;
    }

    @Autowired
    public void setFileDao(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    @Autowired
    public void setClassDao(ClassDao classDao) {
        this.classDao = classDao;
    }

    @Autowired
    public void setFieldDao(FieldDao fieldDao) {
        this.fieldDao = fieldDao;
    }

    @Autowired
    public void setMethodDao(MethodDao methodDao) {
        this.methodDao = methodDao;
    }*/

    public Map<String, Set<PackageInfo>> getPackageInfos() {
        return packageInfos;
    }

    public Map<String, Set<FileInfo>> getFileInfos() {
        return fileInfos;
    }

    public Map<String, Set<ClassInfo>> getClassInfos() {
        return classInfos;
    }

    public Map<String, Set<MethodInfo>> getMethodInfos() {
        return methodInfos;
    }

    public Map<String, Set<FieldInfo>> getFieldInfos() {
        return fieldInfos;
    }

}