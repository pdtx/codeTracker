/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 16:41
 **/
package cn.edu.fudan.codetracker.handler;


import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.RelationShip;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class AnalyzeDiffFile {

    private RepoInfoBuilder curRepoInfo;
    private RepoInfoBuilder preRepoInfo;
    private ProxyDao proxyDao;

    private Map<String, Set<PackageInfo>> packageInfos;
    private Map<String, Set<FileInfo>> fileInfos;
    private Map<String, Set<ClassInfo>> classInfos;
    private Map<String, Set<MethodInfo>> methodInfos;
    private Map<String, Set<FieldInfo>> fieldInfos;
    private Map<String, Set<StatementInfo>> statementInfos;

    /**
     * modifyPackageUuid hash，value 修改后
     */
    private Map<Integer, TrackerInfo> modifyPackageUuid;


    AnalyzeDiffFile(ProxyDao proxyDao, RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo) {
        this.proxyDao = proxyDao;
        this.preRepoInfo = preRepoInfo;
        this.curRepoInfo = curRepoInfo;
        packageInfos = init();
        fileInfos = init();
        classInfos = init();
        methodInfos = init();
        fieldInfos = init();
        statementInfos = init();
        modifyPackageUuid = new HashMap<>();
/*        packageInfos = new HashMap<>();
        fileInfos = new HashMap<>();
        classInfos = new HashMap<>();
        methodInfos = new HashMap<>();
        fieldInfos = new HashMap<>();
        init(packageInfos);
        init(fileInfos);
        init(classInfos);
        init(methodInfos);
        init(fieldInfos);*/
    }

    private <T> Map<String, Set<T>> init() {
        Map<String, Set<T>> map = new HashMap<>();
        Set<T> add = new HashSet<>();
        Set<T> delete = new HashSet<>();
        Set<T> change = new HashSet<>();
        map.put(RelationShip.ADD.name(), add);
        map.put(RelationShip.DELETE.name(), delete);
        map.put(RelationShip.CHANGE.name(), change);
        return map;
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
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.PACKAGE, packageInfo.getModuleName(), packageInfo.getPackageName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo != null) {
                modifyPackageUuid.put(packageInfo.hashCode(), trackerInfo);
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
        statementInfos.get(RelationShip.ADD.name()).addAll(addRepoInfo.getStatementInfos());
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
                if (! modifyPackageUuid.containsKey(hashCode)) {
                    trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.PACKAGE , packageInfo.getModuleName(), packageInfo.getPackageName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                    if (trackerInfo == null) {
                        continue;
                    }
                    modifyPackageUuid.put(hashCode, trackerInfo);
                    packageInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name() , trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                    packageInfos.get(RelationShip.CHANGE.name()).add(packageInfo);
                }
            }
        }

        for (FileInfo fileInfo : deleteRepoInfo.getFileInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FILE, fileInfo.getFilePath(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.ADD.name() , 1, fileInfo.getUuid()));
                fileInfos.get(RelationShip.ADD.name()).add(fileInfo);
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            fileInfos.get(RelationShip.DELETE.name()).add(fileInfo);
        }

        for (ClassInfo classInfo : deleteRepoInfo.getClassInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            classInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            classInfos.get(RelationShip.DELETE.name()).add(classInfo);
        }

        for (MethodInfo methodInfo : deleteRepoInfo.getMethodInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            methodInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            methodInfos.get(RelationShip.DELETE.name()).add(methodInfo);
        }

        for (FieldInfo fieldInfo : deleteRepoInfo.getFieldInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)fieldInfo.getParent()).getFilePath(), ((ClassInfo)fieldInfo.getParent()).getClassName(), fieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            fieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            fieldInfos.get(RelationShip.DELETE.name()).add(fieldInfo);
        }

        for (StatementInfo statementInfo : deleteRepoInfo.getStatementInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT);
            if (trackerInfo == null) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            statementInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            statementInfos.get(RelationShip.DELETE.name()).add(statementInfo);
        }
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
    void modifyInfoConstruction(List<String> fileNameList, List<String> diffPathList) {
        if (diffPathList == null || diffPathList.size() == 0) {
            return;
        }
        String change = RelationShip.CHANGE.name();
        TrackerInfo trackerInfo;
        for (PackageInfo packageInfo : curRepoInfo.getPackageInfos()) {
            int hashCode = packageInfo.hashCode();
            if (! modifyPackageUuid.containsKey(hashCode)) {
                trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.PACKAGE, packageInfo.getModuleName(), packageInfo.getPackageName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                if (trackerInfo == null) {
                    log.error("package tracker Info null! commit:{}, module:{}, package:{} ",curRepoInfo.getCommit(), packageInfo.getModuleName(), packageInfo.getPackageName());
                    continue;
                }
                modifyPackageUuid.put(hashCode, trackerInfo);
                packageInfo.setTrackerInfo(new TrackerInfo(change , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                packageInfos.get(change).add(packageInfo);
            }
        }

        for (FileInfo fileInfo : curRepoInfo.getFileInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FILE, fileInfo.getFilePath(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                log.error("file tracker info null commit:{}, file path:{}", fileInfo.getCommit(), fileInfo.getFilePath());
                continue;
            }
            fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name() , trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
            fileInfos.get(change).add(fileInfo);
        }

        // class 修改
        for (ClassInfo classInfo : curRepoInfo.getClassInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                String errorMessage = "class tracker info null！ commit: " + curRepoInfo.getCommit() +
                        " class name : " + classInfo.getClassName() +
                        " file path : " + classInfo.getFilePath();
                log.error(errorMessage);
                continue;
            }
            classInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name() , trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
            classInfos.get(change).add(classInfo);
        }

        Set<String> uuidList = new HashSet<>();
        boolean isFirst = true;
        for (int i = 0; i < diffPathList.size(); i++) {
            String input ;
            try {
                input = FileUtils.readFileToString(new File(diffPathList.get(i)), "UTF-8");
            }catch (IOException e) {
                log.error(e.getMessage());
                continue;
            }
            String filePath = fileNameList.get(i);
            JSONArray diffDetail = JSONArray.parseArray(input);

            // field
            // add
            if (isFirst) {
                for (FieldInfo curFieldInfo : curRepoInfo.getFieldInfos()) {
                    FieldInfo preFieldInfo = findFieldInfoByName(curFieldInfo.getSimpleName(), preRepoInfo.getFieldInfos(), filePath);
                    if (preFieldInfo == null) {
                        curFieldInfo.setTrackerInfo(RelationShip.ADD.name(), 1, curFieldInfo.getUuid());
                        fieldInfos.get(RelationShip.ADD.name()).add(curFieldInfo);
                        // 减少后续查找时间
                        uuidList.add(curFieldInfo.getUuid());
                    }
                }

                for (FieldInfo preFieldInfo : preRepoInfo.getFieldInfos()) {
                    FieldInfo curFieldInfo = findFieldInfoByName(preFieldInfo.getSimpleName(), curRepoInfo.getFieldInfos(), filePath);
                    trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)preFieldInfo.getParent()).getFilePath(), ((ClassInfo)preFieldInfo.getParent()).getClassName(), preFieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                    if (trackerInfo == null) {
                        log.error("FieldInfo info null! commit:{}, filePath:{}, className:{}, name:{}",curRepoInfo.getCommit(), ((ClassInfo)preFieldInfo.getParent()).getFilePath(), ((ClassInfo)preFieldInfo.getParent()).getClassName(), preFieldInfo.getSimpleName());
                        continue;
                    }
                    if (curFieldInfo == null) {
                        preFieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                        fieldInfos.get(RelationShip.DELETE.name()).add(preFieldInfo);
                    } else if ( !uuidList.contains(curFieldInfo.getUuid())) {
                        curFieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                        fieldInfos.get(RelationShip.CHANGE.name()).add(curFieldInfo);
                    }
                }
                isFirst = false;
            }

            String changeRelation;
            String domainType;
            String description;
            String range;
            int begin,end;
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

                // find class: rename
                if (domainType.equals("ClassOrInterface".toLowerCase())) {
                    switch (changeRelation){
                        case "insert":
                            handleClass(curRepoInfo, filePath, range, RelationShip.ADD.name());
                            break;
                        case "change":
                            //before change
                            try {
                                begin = rangeAnalyzeBegin(range.split("-")[0]);
                                end = rangeAnalyzeEnd(range.split("-")[0]);
                                ClassInfo preClassInfo = findClassInfoByRange(preRepoInfo.getClassInfos(), filePath, begin, end);
                                // 必须得到root uuid 以及 version 否则无法关联起来
                                TrackerInfo preTrackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, preClassInfo.getFilePath(), preClassInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                                //after change
                                begin = rangeAnalyzeBegin(range.split("-")[1]);
                                end = rangeAnalyzeEnd(range.split("-")[1]);
                                ClassInfo curClassInfo = findClassInfoByRange(curRepoInfo.getClassInfos(), filePath, begin, end);
                                if (curClassInfo != null) {
                                    curClassInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), preTrackerInfo.getVersion() + 1, preTrackerInfo.getRootUUID()));
                                    // 直接入库
                                    classInfos.get(RelationShip.CHANGE.name()).add(curClassInfo);
                                }
                            }catch (Exception e) {
                                log.error(e.getMessage());
                            }
                            break;
                        case "delete":
                            handleClass(preRepoInfo, filePath, range, RelationShip.DELETE.name());
                            break;
                        case "move":
                            // method move 不作处理
                            break;
                        default:
                            log.error("relation error method " + changeRelation);
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
                                log.error("preMethodInfo NULL ERROR: " + filePath + ";  range: " + range);
                                preMethodInfo = findMethodInfoByRange(preRepoInfo.getMethodInfos(), filePath, begin, end);
                                break;
                            }
                            try {
                                // 必须得到root uuid 以及 version 否则无法关联起来
                                trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)preMethodInfo.getParent()).getFilePath(), ((ClassInfo)preMethodInfo.getParent()).getClassName(), preMethodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                                //after change
                                begin = rangeAnalyzeBegin(range.split("-")[1]);
                                end = rangeAnalyzeEnd(range.split("-")[1]);
                                MethodInfo curMethodInfo = findMethodInfoByRange(curRepoInfo.getMethodInfos(), filePath, begin, end);
                                if (curMethodInfo != null) {
                                    if (trackerInfo == null) {
                                        curMethodInfo.setTrackerInfo(new TrackerInfo(RelationShip.ADD.name(), 1, curMethodInfo.getUuid()));
                                        methodInfos.get(RelationShip.ADD.name()).add(curMethodInfo);
                                        break;
                                    }
                                    curMethodInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                                    curMethodInfo.getDiff().getJSONArray("data").add(oneDiff);
                                    // 直接入库
                                    methodInfos.get(RelationShip.CHANGE.name()).add(curMethodInfo);
                                }
                            }catch (NullPointerException e) {
                                e.printStackTrace();
                                log.error("method change declaration ：range:{}" , range );
                            }

                            break;
                        case "delete":
                            handleMethod(preRepoInfo.getMethodInfos(), filePath, range, RelationShip.DELETE.name());
                            break;
                        case "move":
                            break;
                        default :
                            log.error("method relation error,relation:{}" ,changeRelation);
                    }
                }
            }

            for (int j = 0; j < diffDetail.size(); j++) {
                JSONObject oneDiff = diffDetail.getJSONObject(j);
                domainType = oneDiff.getString("type1").toLowerCase();
                changeRelation = oneDiff.getString("type2").toLowerCase();
                // Change.Move 是比statement 更细粒度的语句的change
                if (changeRelation.contains("move")) {
                    changeRelation = "change";
                }
                //description = oneDiff.getString("description");
                range = oneDiff.getString("range");

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
                            if (preMethodInfo == null) {
                                log.error("preMethodInfo NULL");
                                break;
                            }
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
                                log.error(e.getMessage());
                                log.error("statement range lack! " + diffPathList.get(i) + "  range:" + range);
                            }
                            break;
                        default:
                                log.error("statement relation error  " + changeRelation );
                    }
                    if (curMethodInfo != null && !methodInfos.get(RelationShip.CHANGE.name()).contains(curMethodInfo) &&
                            !methodInfos.get(RelationShip.ADD.name()).contains(curMethodInfo)) {
                        trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)curMethodInfo.getParent()).getFilePath(), ((ClassInfo)curMethodInfo.getParent()).getClassName(), curMethodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                        if (trackerInfo == null) {
                            log.error("method trackerInfo null！ commit:{}, class name:{}, signature:{}", curRepoInfo.getCommit(), ((ClassInfo)curMethodInfo.getParent()).getClassName(), curMethodInfo.getSignature());
                            continue;
                        }
                        curMethodInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                        curMethodInfo.getDiff().getJSONArray("data").add(oneDiff);
                        methodInfos.get(RelationShip.CHANGE.name()).add(curMethodInfo);
                        //classInfos.get(RelationShip.CHANGE.name()).add();
                    }
                }
            }
        }

    }

//    private void diffClassAnalyze(JSONArray diffDetail, String filePath, RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo) {
//        String changeRelation;
//        String domainType;
//        String range;
//        int begin,end;
//        for (int j = 0; j < diffDetail.size(); j++) {
//            JSONObject oneDiff = diffDetail.getJSONObject(j);
//            domainType = oneDiff.getString("type1").toLowerCase();
//            changeRelation = oneDiff.getString("type2").toLowerCase();
//            // Change.Move 是比statement 更细粒度的语句的change
//            if ("change.move".equals(changeRelation)) {
//                changeRelation = "change";
//            }
//            range = oneDiff.getString("range");
//
//            // find class: rename
//            if (domainType.equals("ClassOrInterface".toLowerCase())) {
//                switch (changeRelation){
//                    case "insert":
//                        handleClass(curRepoInfo, filePath, range, RelationShip.ADD.name());
//                        break;
//                    case "change":
//                        //before change
//                        begin = rangeAnalyzeBegin(range.split("-")[0]);
//                        end = rangeAnalyzeEnd(range.split("-")[0]);
//                        ClassInfo preClassInfo = findClassInfoByRange(preRepoInfo.getClassInfos(), filePath, begin, end);
//                        // 必须得到root uuid 以及 version 否则无法关联起来
//                        TrackerInfo preTrackerInfo = classDao.getTrackerInfo(preClassInfo.getFilePath(), preClassInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
//                        //after change
//                        begin = rangeAnalyzeBegin(range.split("-")[1]);
//                        end = rangeAnalyzeEnd(range.split("-")[1]);
//                        ClassInfo curClassInfo = findClassInfoByRange(curRepoInfo.getClassInfos(), filePath, begin, end);
//                        if (curClassInfo != null) {
//                            curClassInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), preTrackerInfo.getVersion() + 1, preTrackerInfo.getRootUUID()));
//                            // 直接入库
//                            classInfos.get(RelationShip.CHANGE.name()).add(curClassInfo);
//                            //curRepoInfo.getClassInfos().remove(curClassInfo);
//                        }
//                        break;
//                    case "delete":
//                        handleClass(preRepoInfo, filePath, range, RelationShip.DELETE.name());
//                        break;
//                    default:
//                        log.error("method changeRelation mapping failed");
//                }
//            }
//
//        }
//    }

    private FieldInfo findFieldInfoByName(String fieldName, List<FieldInfo> fieldInfos, String filePath) {
        for (FieldInfo fieldInfo : fieldInfos) {
            if (filePath.equals(((ClassInfo)fieldInfo.getParent()).getFilePath()) && fieldName.equals(fieldInfo.getSimpleName())) {
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
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            methodInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            methodInfos.get(RelationShip.DELETE.name()).add(methodInfo);
        }

    }

    private MethodInfo findMethodInfoByRange( List<MethodInfo> methodInfos, String filePath,int begin, int end) {
        for (MethodInfo methodInfo : methodInfos) {
            boolean isInclude = !(methodInfo.getBegin() > end || methodInfo.getEnd() < begin);
            boolean isSameFilePath = filePath.contains(((ClassInfo)methodInfo.getParent()).getFilePath()) || ((ClassInfo)methodInfo.getParent()).getFilePath().contains(filePath);
            if (isInclude && isSameFilePath) {
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
        final int firstVersion = 1;

        if (relation.equals(RelationShip.ADD.name())) {
            classInfo.setTrackerInfo(new TrackerInfo(relation, firstVersion, classInfo.getUuid()));
            classInfos.get(relation).add(classInfo);
            for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                methodInfo.setTrackerInfo(new TrackerInfo(relation, firstVersion, classInfo.getUuid()));
                methodInfos.get(relation).add(methodInfo);
            }
            for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                fieldInfo.setTrackerInfo(new TrackerInfo(relation, firstVersion, classInfo.getUuid()));
                fieldInfos.get(relation).add(fieldInfo);
            }
        } else {
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            classInfo.setTrackerInfo(new TrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            classInfos.get(relation).add(classInfo);

            for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                methodInfo.setTrackerInfo(new TrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                methodInfos.get(relation).add(methodInfo);
            }

            for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)fieldInfo.getParent()).getFilePath(), ((ClassInfo)fieldInfo.getParent()).getClassName(), fieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                fieldInfo.setTrackerInfo(new TrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                fieldInfos.get(relation).add(fieldInfo);
            }
        }
    }

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

    public Map<String, Set<StatementInfo>> getStatementInfos() {
        return statementInfos;
    }

}