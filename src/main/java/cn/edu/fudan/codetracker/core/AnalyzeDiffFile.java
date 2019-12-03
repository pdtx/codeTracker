/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 16:41
 **/
package cn.edu.fudan.codetracker.core;


import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.RelationShip;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

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
        Map<String, Set<T>> map = new HashMap<>(16);
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
     *   删除的数据也作为一条插入
     *   基本数据和删除的前一样 relation 为delete表示在这个版本删除 删除人是谁 相关的删除信息等
     *   用curRepoInfo 来初始化 表示在current 这个版本删除
     * */
    void deleteInfoConstruction(List<String> deleteFilesList) {
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
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, statementInfo.getMethodUuid(), String.valueOf(statementInfo.getBegin()), String.valueOf(statementInfo.getEnd()), statementInfo.getBody());
            if (trackerInfo == null) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            statementInfo.setTrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID());
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
    /*void modifyInfoConstruction1(List<String> fileNameList, List<String> diffPathList) {
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
                                findMethodInfoByRange(preRepoInfo.getMethodInfos(), filePath, begin, end);
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

    }*/

    private FieldInfo findFieldInfoByName(String fieldName, List<FieldInfo> fieldInfos, String filePath) {
        for (FieldInfo fieldInfo : fieldInfos) {
            if (filePath.equals(((ClassInfo)fieldInfo.getParent()).getFilePath()) && fieldName.equals(fieldInfo.getSimpleName())) {
                return fieldInfo;
            }
        }
        return null;
    }

    private MethodInfo handleMethod(List<MethodInfo> methodInfoList, String range, String relation) {
        if (methodInfoList == null || methodInfoList.size() == 0) {
            log.error("methodInfoList is null");
            return null;
        }
        int begin = rangeAnalyzeBegin(range);
        int end = rangeAnalyzeEnd(range);
        MethodInfo methodInfo = findMethodInfoByRange(methodInfoList, begin, end);
        if (methodInfo == null) {
            return null;
        }
        // delete: 只有changeRelation 改变
        if (relation.equals(RelationShip.DELETE.name())) {
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            methodInfo.setTrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID());
        }
        methodInfos.get(relation).add(methodInfo);
        handleStatement(castBaseInfo(methodInfo.getChildren()), relation);
        return methodInfo;
    }

    private void handleStatement(List<StatementInfo> statementInfos, String relation) {
        if (relation.equals(RelationShip.DELETE.name())) {
            for (StatementInfo statementInfo : statementInfos) {
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, statementInfo.getMethodUuid(), String.valueOf(statementInfo.getBegin()), String.valueOf(statementInfo.getEnd()), statementInfo.getBody());
                statementInfo.getTrackerInfo().setChangeRelation(relation);
                statementInfo.getTrackerInfo().setRootUUID(trackerInfo.getRootUUID());
                statementInfo.getTrackerInfo().setVersion(trackerInfo.getVersion());
                this.statementInfos.get(relation).add(statementInfo);
                handleStatement(castBaseInfo(statementInfo.getChildren()), relation);
            }
            return;
        }
        if (relation.equals(RelationShip.ADD.name())) {
            this.statementInfos.get(relation).addAll(statementInfos);
            for (StatementInfo statementInfo : statementInfos) {
                handleStatement(castBaseInfo(statementInfo.getChildren()), relation);
            }
        }
    }

/*    @SuppressWarnings("unchecked")
    private List<StatementInfo> castBaseInfoToStatementInfo(List<? extends BaseInfo> baseInfos) {
        try {
            if (baseInfos == null || baseInfos.size() == 0) {
                return null;
            }
            if (baseInfos.get(0) instanceof StatementInfo) {
                return (List<StatementInfo>) baseInfos;
            }
        }catch (ClassCastException e){
            log.error(e.getMessage());
        }
        return null;
    }*/

    private MethodInfo findMethodInfoByRange( List<MethodInfo> methodInfos ,int begin, int end) {
        for (MethodInfo methodInfo : methodInfos) {
            // !(methodInfo.getBegin() > end || methodInfo.getEnd() < begin);
            if (methodInfo.getBegin() <= end && methodInfo.getEnd() >= begin) {
                return methodInfo;
            }
        }
        return null;
    }

    /**
     * fileInfoExtractor 查找的更准确，但是package信息不准确
     *
     * */
    private void handleClass(List<ClassInfo> classInfoList , String range, String relation) {
        //根据range当前版本的method
        int begin = rangeAnalyzeBegin(range);
        int end = rangeAnalyzeEnd(range);
        ClassInfo classInfo = findClassInfoByRange(classInfoList, begin, end);
        if (classInfo != null) {
            if (relation.equals(RelationShip.ADD.name())) {
                classInfos.get(relation).add(classInfo);
                methodInfos.get(relation).addAll(classInfo.getMethodInfos());
                for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                    handleStatement(castBaseInfo(methodInfo.getChildren()), relation);
                }
                fieldInfos.get(relation).addAll(classInfo.getFieldInfos());
            } else if (relation.equals(RelationShip.DELETE.name())){
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                classInfo.setTrackerInfo( relation , trackerInfo.getVersion(), trackerInfo.getRootUUID());
                classInfos.get(relation).add(classInfo);

                for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                    trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                    methodInfo.setTrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID());
                    methodInfos.get(relation).add(methodInfo);
                    handleStatement(castBaseInfo(methodInfo.getChildren()), relation);
                }

                for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                    trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)fieldInfo.getParent()).getFilePath(), ((ClassInfo)fieldInfo.getParent()).getClassName(), fieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                    fieldInfo.setTrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID());
                    fieldInfos.get(relation).add(fieldInfo);
                }
            }
        }
    }

    private ClassInfo findClassInfoByRange(List<ClassInfo> classInfos, int begin, int end) {
        if (classInfos == null || classInfos.size() == 0) {
            return null;
        }
        for (ClassInfo classInfo : classInfos) {
            // !(classInfo.getBegin() > end || classInfo.getEnd() < begin)
            if (classInfo.getBegin() <= end && classInfo.getEnd() >= begin) {
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

    void modifyInfoConstruction(List<String> fileNameList, List<String> diffPathList) {
        if (diffPathList == null || diffPathList.size() == 0) {
            return;
        }
        TrackerInfo trackerInfo;
        // package file class
        handleModification();
        Set<String> fieldUuidList = new HashSet<>();
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
            FileInfo preFileInfo = findFileInfoByPath(preRepoInfo, filePath);
            FileInfo curFileInfo = findFileInfoByPath(curRepoInfo, filePath);
            if (preFileInfo == null || curFileInfo == null) {
                log.error("fileInfo is null.file path:{}", filePath);
                continue;
            }

            if (isFirst) {
                // add field
                for (FieldInfo curFieldInfo : curRepoInfo.getFieldInfos()) {
                    FieldInfo preFieldInfo = findFieldInfoByName(curFieldInfo.getSimpleName(), preRepoInfo.getFieldInfos(), filePath);
                    if (preFieldInfo == null) {
                        curFieldInfo.setTrackerInfo(RelationShip.ADD.name(), 1, curFieldInfo.getUuid());
                        fieldInfos.get(RelationShip.ADD.name()).add(curFieldInfo);
                        // 减少后续查找时间
                        fieldUuidList.add(curFieldInfo.getUuid());
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
                    } else if ( !fieldUuidList.contains(curFieldInfo.getUuid())) {
                        curFieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
                        fieldInfos.get(RelationShip.CHANGE.name()).add(curFieldInfo);
                    }
                }
                isFirst = false;
            }

            List<MethodInfo> curMethodInfoList = getMethodInfoListByFileInfo(curFileInfo);
            List<MethodInfo> preMethodInfoList = getMethodInfoListByFileInfo(preFileInfo);
            List<StatementInfo> curStatementInfoList = getStatementInfoListByFileInfo(curMethodInfoList);
            List<StatementInfo> preStatementInfoList = getStatementInfoListByFileInfo(preMethodInfoList);
            for (int j = 0; j < diffDetail.size(); j++) {
                JSONObject oneDiff = diffDetail.getJSONObject(j);
                String domainType = oneDiff.getString("type1").toLowerCase();
                String description = oneDiff.getString("description");
                if (("classorinterface").equals(domainType)) {
                    analyzeModifiedClass(oneDiff, preFileInfo, curFileInfo);
                    continue;
                }
                // method change declaration
                if ("member".equals(domainType) && description.toLowerCase().contains("method")) {
                    analyzeModifiedMethod(oneDiff, curMethodInfoList, preMethodInfoList);
                    continue;
                }
                if ("statement".equals(domainType)) {
                    analyzeModifiedStatement(oneDiff, preStatementInfoList, curStatementInfoList);
                }
            }
        }
    }

    private FileInfo findFileInfoByPath(RepoInfoBuilder curRepoInfo, String filePath) {
        for (FileInfo fileInfo : curRepoInfo.getFileInfos()) {
            if (fileInfo.getFilePath().contains(filePath) || filePath.contains(fileInfo.getFilePath())) {
                return fileInfo;
            }
        }
        return null;
    }

    private void analyzeModifiedStatement(JSONObject oneDiff, List<StatementInfo> preStatementInfoList, List<StatementInfo> curStatementInfoList) {
        String changeRelation = oneDiff.getString("type2");
        // Change.Move 是比statement 更细粒度的语句的change
/*        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            changeRelation = ChangeEntityDesc.StageIIOpt.OPT_CHANGE;
        }*/
        String range = oneDiff.getString("range");
        // statement
        StatementInfo statementInfo = null;
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(changeRelation)) {
            int begin = rangeAnalyzeBegin(range);
            int end = rangeAnalyzeEnd(range);
            statementInfo = findStatementInfoByRange(curStatementInfoList, begin, end);
            List<StatementInfo> addStat = new ArrayList<>(1);
            addStat.add(statementInfo);
            handleStatement(addStat, RelationShip.ADD.name());
        }

        if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(changeRelation)) {
            int begin = rangeAnalyzeBegin(range);
            int end = rangeAnalyzeEnd(range);
            statementInfo = findStatementInfoByRange(preStatementInfoList, begin, end);
            List<StatementInfo> deleteStat = new ArrayList<>(1);
            deleteStat.add(statementInfo);
            handleStatement(deleteStat, RelationShip.DELETE.name());
        }

        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(changeRelation) || ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(changeRelation) || ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            try {
                int begin = rangeAnalyzeBegin(range.split("-")[0]);
                int end = rangeAnalyzeEnd(range.split("-")[0]);
                StatementInfo preStat = findStatementInfoByRange(preStatementInfoList, begin, end);
                Assert.notNull(preStat, "change: preStat is null!");
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, preStat.getMethodUuid(), String.valueOf(preStat.getBegin()), String.valueOf(preStat.getEnd()), preStat.getBody());
                begin = rangeAnalyzeBegin(range.split("-")[1]);
                end = rangeAnalyzeEnd(range.split("-")[1]);
                // 根据begin 与 end 找到 statement
                StatementInfo curStat = findStatementInfoByRange(curStatementInfoList, begin, end);
                Assert.notNull(curStat, "change: curStat is null!");
                curStat.setTrackerInfo(changeRelation, trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                statementInfos.get(changeRelation).add(curStat);
            }catch (ArrayIndexOutOfBoundsException e) {
                log.error(e.getMessage());
                log.error("statement range lack! range:{}", range);
            }
        }

        if (statementInfo != null) {
            backtracking(statementInfo.getParent());
        }
    }

    private StatementInfo findStatementInfoByRange(List<StatementInfo> statementInfoList, int begin, int end) {
        StatementInfo result = null;
        int r  = -1;
        for (StatementInfo statementInfo : statementInfoList) {
            // 先找父statement
            if (statementInfo.getBegin() == begin && statementInfo.getEnd() == end) {
                return statementInfo;
            }
            if (statementInfo.getBegin() <= end && statementInfo.getEnd() >= begin &&
                    (result == null ||  r >  (Math.abs(statementInfo.getEnd() - end) +  Math.abs(statementInfo.getBegin() - begin)))) {
                result = statementInfo;
                r = Math.abs(result.getEnd() - end) +  Math.abs(result.getBegin() - begin);
            }
        }
        return result;
    }

    private List<StatementInfo> getStatementInfoListByFileInfo(List<MethodInfo> methodInfoList) {
        List<StatementInfo> statementInfoList = new ArrayList<>();
        for (MethodInfo methodInfo : methodInfoList) {
            List<StatementInfo> s = castBaseInfo(methodInfo.getChildren());
            if (s == null || s.size() == 0) {
                continue;
            }
            travel(s , statementInfoList);
        }
        return statementInfoList;
    }

    private void travel(List<StatementInfo> s, List<StatementInfo> result) {
        if (s == null || s.size() == 0) {
            return;
        }
        result.addAll(s);
        for (StatementInfo statementInfo : s) {
            List<StatementInfo> children = castBaseInfo(statementInfo.getChildren());
            if (children == null || children.size() == 0) {
                continue;
            }
            travel(children, result);
        }
    }

    private void analyzeModifiedMethod(JSONObject oneDiff , List<MethodInfo> curMethodInfoList, List<MethodInfo> preMethodInfoList) {
        String changeRelation = oneDiff.getString("type2");
        // Change.Move 是比statement 更细粒度的语句的change
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            changeRelation = ChangeEntityDesc.StageIIOpt.OPT_CHANGE;
        }
        String range = oneDiff.getString("range");
        MethodInfo methodInfo = null;
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(changeRelation)) {
            methodInfo = handleMethod(curMethodInfoList, range, RelationShip.ADD.name());
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(changeRelation)) {
            methodInfo = handleMethod(preMethodInfoList, range, RelationShip.DELETE.name());
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(changeRelation)) {
            //before change
            int begin = rangeAnalyzeBegin(range.split("-")[0]);
            int end = rangeAnalyzeEnd(range.split("-")[0]);
            MethodInfo preMethodInfo = findMethodInfoByRange(preMethodInfoList, begin, end);
            if (preMethodInfo == null) {
                log.error("preMethodInfo NULL ERROR: ");
                // debug
                findMethodInfoByRange(curMethodInfoList, begin, end);
                return;
            }
            try {
                // 必须得到root uuid 以及 version 否则无法关联起来
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)preMethodInfo.getParent()).getFilePath(), ((ClassInfo)preMethodInfo.getParent()).getClassName(), preMethodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                //after change
                begin = rangeAnalyzeBegin(range.split("-")[1]);
                end = rangeAnalyzeEnd(range.split("-")[1]);
                MethodInfo curMethodInfo = findMethodInfoByRange(curMethodInfoList, begin, end);
                if (curMethodInfo != null) {
                    if (trackerInfo == null) {
                        methodInfos.get(RelationShip.ADD.name()).add(curMethodInfo);
                        backtracking(curMethodInfo.getParent());
                        return;
                    }
                    curMethodInfo.setTrackerInfo(RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                    curMethodInfo.getDiff().getJSONArray("data").add(oneDiff);
                    // 直接入库
                    methodInfos.get(RelationShip.CHANGE.name()).add(curMethodInfo);
                    methodInfo = curMethodInfo;
                }
            }catch (NullPointerException e) {
                e.printStackTrace();
                log.error("method change declaration ：range:{}" , range );
            }
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(changeRelation)) {
            return;
        }
        if (methodInfo != null) {
            backtracking(methodInfo.getParent());
        }
        log.error("method relation error,relation:{}" ,changeRelation);
    }

    private void backtracking(BaseInfo parent) {
        String change = RelationShip.CHANGE.name();
        if (parent instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) parent;
            if (! classInfos.get(change).contains(classInfo)) {
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                Assert.notNull(trackerInfo, "backtracking classInfo's tracker info is null! path: " + classInfo.getFilePath() + ",name: "+  classInfo.getClassName());
                classInfo.setTrackerInfo(change, trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                classInfos.get(change).add(classInfo);
            }
            return;
        }

        if (parent instanceof MethodInfo) {
            MethodInfo methodInfo = (MethodInfo) parent;
            if (! methodInfos.get(change).contains(methodInfo)) {
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                Assert.notNull(trackerInfo, "backtracking methodInfo's tracker info is null! path: " + ((ClassInfo)methodInfo.getParent()).getFilePath() + ",name: "+  methodInfo.getSignature());
                methodInfo.setTrackerInfo(change, trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                methodInfos.get(change).add(methodInfo);
                backtracking(methodInfo.getParent());
            }
            return;
        }

        if (parent instanceof StatementInfo) {
            StatementInfo statementInfo  = (StatementInfo) parent;
            if (! statementInfos.get(change).contains(statementInfo)) {
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, statementInfo.getMethodUuid(), String.valueOf(statementInfo.getBegin()), String.valueOf(statementInfo.getEnd()), statementInfo.getBody());
                Assert.notNull(trackerInfo, "backtracking statementInfo's tracker info is null!" );
                statementInfo.setTrackerInfo(change, trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                statementInfos.get(change).add(statementInfo);
                backtracking(statementInfo.getParent());
            }
        }
    }

    private List<MethodInfo> getMethodInfoListByFileInfo(FileInfo fileInfo) {
        List<MethodInfo> methodInfoList = new ArrayList<>();
        List<ClassInfo> classInfoList = castBaseInfo(fileInfo.getChildren());
        Assert.notNull(classInfoList, "ERROR! analyzeModifiedMethod: curClassInfoList is null");
        for (ClassInfo classInfo : classInfoList) {
            methodInfoList.addAll(classInfo.getMethodInfos());
        }
        return methodInfoList;
    }

    private void analyzeModifiedClass(JSONObject oneDiff, FileInfo preFileInfo, FileInfo curFileInfo) {
        String changeRelation = oneDiff.getString("type2");
        // Change.Move 是比statement 更细粒度的语句的change
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            changeRelation = ChangeEntityDesc.StageIIOpt.OPT_CHANGE;
        }

        String range = oneDiff.getString("range");
        String filePath = curFileInfo.getFilePath();
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(changeRelation)) {
            handleClass(castBaseInfo(curFileInfo.getChildren()), range, RelationShip.ADD.name());
            return;
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(changeRelation)) {
            try {
                int begin = rangeAnalyzeBegin(range.split("-")[0]);
                int end = rangeAnalyzeEnd(range.split("-")[0]);
                List<ClassInfo> classInfoList = castBaseInfo(preFileInfo.getChildren());
                if (classInfoList == null) {
                    log.error("castBaseInfo classInfoList is null,file path :{}", filePath);
                    return;
                }
                ClassInfo preClassInfo = findClassInfoByRange(classInfoList, begin, end);
                Assert.notNull(preClassInfo, "analyzeModifiedClass,change situation ! preClassInfo is null");
                // 必须得到root uuid 以及 version 否则无法关联起来
                TrackerInfo preTrackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, preClassInfo.getFilePath(), preClassInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                //after change
                begin = rangeAnalyzeBegin(range.split("-")[1]);
                end = rangeAnalyzeEnd(range.split("-")[1]);
                classInfoList = castBaseInfo(curFileInfo.getChildren());
                if (classInfoList == null) {
                    log.error("castBaseInfo classInfoList is null,file path :{}", filePath);
                    return;
                }
                ClassInfo curClassInfo = findClassInfoByRange(classInfoList, begin, end);
                if (curClassInfo != null) {
                    curClassInfo.setTrackerInfo(RelationShip.CHANGE.name(), preTrackerInfo.getVersion() + 1, preTrackerInfo.getRootUUID());
                    // 直接入库
                    classInfos.get(RelationShip.CHANGE.name()).add(curClassInfo);
                }
            }catch (Exception e) {
                log.error(e.getMessage());
            }
            return;
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(changeRelation)) {
            handleClass(castBaseInfo(preFileInfo.getChildren()), range, RelationShip.DELETE.name());
            return;
        }
        // method move 不处理
        if (ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(changeRelation)) {
            return;
        }
        log.error("relation error method " + changeRelation);
    }


    @SuppressWarnings("unchecked")
    private <T> List<T> castBaseInfo(List<? extends BaseInfo> baseInfos) {
        try {
            if (baseInfos == null || baseInfos.size() == 0) {
                return null;
            }
            return (List<T>) baseInfos;
        }catch (ClassCastException e){
            log.error(e.getMessage());
        }
        return null;
    }

    private void handleModification() {
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