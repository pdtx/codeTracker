/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 16:41
 **/
package cn.edu.fudan.codetracker.core;


import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.RelationShip;
import cn.edu.fudan.codetracker.domain.diff.DiffInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.exception.ExceptionMessage;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import com.alibaba.fastjson.JSONArray;
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
    private Map<String, String> methodUuidMap;

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
        // key 是当前method 的uuid value 是meta_method uuid
        methodUuidMap = new HashMap<>();
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
    void addInfoConstruction(RepoInfoBuilder addRepoInfo) {
        // 更新packageUUID
        for (PackageInfo packageInfo : addRepoInfo.getPackageInfos()) {
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.PACKAGE, packageInfo.getModuleName(), packageInfo.getPackageName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo != null) {
                modifyPackageUuid.put(packageInfo.hashCode(), trackerInfo);
                packageInfo.setTrackerInfo(new TrackerInfo( RelationShip.CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
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
     *   用curRepoInfo 来初始化 表示在current 这个版本删除； 数据库中 change relation 为delete字段表示在当前版本被删除
     *   其中 commit committer commitDate commitMessage 都为当前版本的信息
     * */
    void deleteInfoConstruction(RepoInfoBuilder deleteRepoInfo) {
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
            if (trackerInfoNullHandler(trackerInfo, ExceptionMessage.FILE_TRACKER_INFO_NULL)) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            fileInfos.get(RelationShip.DELETE.name()).add(fileInfo);
        }

        for (ClassInfo classInfo : deleteRepoInfo.getClassInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfoNullHandler(trackerInfo, ExceptionMessage.CLASS_TRACKER_INFO_NULL)) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            classInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            classInfos.get(RelationShip.DELETE.name()).add(classInfo);
        }

        for (MethodInfo methodInfo : deleteRepoInfo.getMethodInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfoNullHandler(trackerInfo, ExceptionMessage.METHOD_TRACKER_INFO_NULL)) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            methodInfo.setTrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID());
            methodUuidMap.put(methodInfo.getUuid(), trackerInfo.getRootUUID());
            methodInfos.get(RelationShip.DELETE.name()).add(methodInfo);
        }

        for (FieldInfo fieldInfo : deleteRepoInfo.getFieldInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)fieldInfo.getParent()).getFilePath(), ((ClassInfo)fieldInfo.getParent()).getClassName(), fieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfoNullHandler(trackerInfo, ExceptionMessage.FIELD_TRACKER_INFO_NULL)) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            fieldInfo.setTrackerInfo(new TrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
            fieldInfos.get(RelationShip.DELETE.name()).add(fieldInfo);
        }

        for (StatementInfo statementInfo : deleteRepoInfo.getStatementInfos()) {
            statementInfo.setMethodUuid(methodUuidMap.get(statementInfo.getMethodUuid()));
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, statementInfo.getMethodUuid(), statementInfo.getBody());
            if (trackerInfoNullHandler(trackerInfo, ExceptionMessage.STATEMENT_TRACKER_INFO_NULL)) {
                continue;
            }
            trackerInfo.setChangeRelation(relation);
            statementInfo.setTrackerInfo(RelationShip.DELETE.name() , trackerInfo.getVersion(), trackerInfo.getRootUUID());
            statementInfos.get(RelationShip.DELETE.name()).add(statementInfo);
        }
    }

    /**
     *   no matter how，package、file will always be "change"
     *   according to diffPath
     * */
    void modifyInfoConstruction(List<String> fileNameList, List<String> diffPathList) {
        if (diffPathList == null || diffPathList.size() == 0) {
            return;
        }
        // package file
        handleModification();
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
            List<ClassInfo> curClassInfoList = castBaseInfo(curFileInfo.getChildren());
            List<ClassInfo> preClassInfoList = castBaseInfo(preFileInfo.getChildren());
            List<MethodInfo> curMethodInfoList = getMethodInfoListByFileInfo(curClassInfoList);
            List<MethodInfo> preMethodInfoList = getMethodInfoListByFileInfo(preClassInfoList);
            List<FieldInfo> curFieldInfoList = getFieldInfoListByFileInfo(curClassInfoList);
            List<FieldInfo> preFieldInfoList = getFieldInfoListByFileInfo(preClassInfoList);
            List<StatementInfo> curStatementInfoList = getStatementInfoListByFileInfo(curMethodInfoList);
            List<StatementInfo> preStatementInfoList = getStatementInfoListByFileInfo(preMethodInfoList);
            // 处理diffDetail
            DiffInfo diffInfo = new DiffInfo(diffDetail);
            for (DiffInfo.OneDiff oneDiff : diffInfo.getDiffInfo().get(ProjectInfoLevel.CLASS)) {
                analyzeModifiedClass(oneDiff, preFileInfo, curFileInfo);
            }
            for (DiffInfo.OneDiff oneDiff : diffInfo.getDiffInfo().get(ProjectInfoLevel.METHOD)) {
                analyzeModifiedMethod(oneDiff, curMethodInfoList, preMethodInfoList, curClassInfoList, preClassInfoList);
            }
            for (MethodInfo methodInfo : curMethodInfoList) {
                MethodInfo preMethodInfo = findMethodInfoBySignature(methodInfo, preMethodInfoList);
                methodInfo.setMappingNode(preMethodInfo);
                if (preMethodInfo != null) {
                    preMethodInfo.setMappingNode(methodInfo);
                }
            }
            for (DiffInfo.OneDiff oneDiff : diffInfo.getDiffInfo().get(ProjectInfoLevel.FIELD)) {
                analyzeModifiedField(oneDiff, curFieldInfoList, preFieldInfoList, curClassInfoList, preClassInfoList);
            }
            for (DiffInfo.OneDiff oneDiff : diffInfo.getDiffInfo().get(ProjectInfoLevel.STATEMENT)) {
                analyzeModifiedStatement(oneDiff, preStatementInfoList, curStatementInfoList, preMethodInfoList, curMethodInfoList);
            }
            unMappedHandle(preStatementInfoList, curStatementInfoList);
        }
    }

    private MethodInfo findMethodInfoBySignature(MethodInfo targetMethodInfo, List<MethodInfo> methodInfoList) {
        String signature = targetMethodInfo.getSignature();
        for (MethodInfo methodInfo : methodInfoList) {
            if (signature.equals(methodInfo.getSignature())) {
                return methodInfo;
            }
        }
        return null;
    }

    private void unMappedHandle(List<StatementInfo> preStatementInfoList, List<StatementInfo> curStatementInfoList) {
        for (StatementInfo statementInfo : preStatementInfoList) {
            if (! statementInfo.isMapped()) {
                backtrackingMethod(statementInfo);
                statementInfo.setMethodUuid(methodUuidMap.get(statementInfo.getMethodUuid()));
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, statementInfo.getMethodUuid(), statementInfo.getBody());
                if (trackerInfo == null) {
                    continue;
                }
                resetBaseInfo(statementInfo);
                statementInfo.setTrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID());
                statementInfos.get(RelationShip.DELETE.name()).add(statementInfo);
            }
        }

        for (StatementInfo statementInfo : curStatementInfoList) {
            if (! statementInfo.isMapped()) {
                backtrackingMethod(statementInfo);
                statementInfo.setMethodUuid(methodUuidMap.get(statementInfo.getMethodUuid()));
                statementInfos.get(RelationShip.ADD.name()).add(statementInfo);
            }
        }
    }

    private void analyzeModifiedClass(DiffInfo.OneDiff oneDiff, FileInfo preFileInfo, FileInfo curFileInfo) {
        String changeRelation = oneDiff.getChangeRelation();
        // Change.Move 是比statement 更细粒度的语句的change
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            changeRelation = ChangeEntityDesc.StageIIOpt.OPT_CHANGE;
        }

        String range = oneDiff.getRange();
        String filePath = curFileInfo.getFilePath();
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(changeRelation)) {
            handleClass(castBaseInfo(curFileInfo.getChildren()), range, RelationShip.ADD.name());
            return;
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(changeRelation)) {
            handleClass(castBaseInfo(preFileInfo.getChildren()), range, RelationShip.DELETE.name());
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
                if (preClassInfo == null) {
                    log.error("analyzeModifiedClass,change situation ! preClassInfo is null");
                    return;
                }
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
                    curClassInfo.setTrackerInfo(RelationShip.SELF_CHANGE.name(), preTrackerInfo.getVersion() + 1, preTrackerInfo.getRootUUID());
                    // 直接入库
                    classInfos.get(RelationShip.CHANGE.name()).add(curClassInfo);
                    curClassInfo.setMapped(true);
                    preClassInfo.setMapped(true);
                }
            }catch (Exception e) {
                log.error(e.getMessage());
            }
            return;
        }
        // method move 不处理
        if (ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(changeRelation)) {
            return;
        }
        log.error("relation error method " + changeRelation);
    }

    private void analyzeModifiedMethod(DiffInfo.OneDiff oneDiff, List<MethodInfo> curMethodInfoList, List<MethodInfo> preMethodInfoList, List<ClassInfo> curClassInfoList, List<ClassInfo> preClassInfoList) {
        String changeRelation = oneDiff.getChangeRelation();
        // Change.Move 是比statement 更细粒度的语句的change
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            changeRelation = ChangeEntityDesc.StageIIOpt.OPT_CHANGE;
        }

        String range = oneDiff.getRange();
        MethodInfo methodInfo ;
        BaseInfo preClassInfo = null,curClassInfo = null;
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(changeRelation)) {
            methodInfo = handleMethod(curMethodInfoList, range, RelationShip.ADD.name());
            Assert.notNull(methodInfo, "OPT_INSERT: method info is null");
            curClassInfo = methodInfo.getParent();

            // 后续修改 正确性有待考究
            preClassInfo = findClassInfoByRange(preClassInfoList, methodInfo.getBegin(), methodInfo.getEnd());
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(changeRelation)) {
            methodInfo = handleMethod(preMethodInfoList, range, RelationShip.DELETE.name());
            if (methodInfo == null) {
                log.error("OPT_DELETE: method info is null");
                return;
            }
            preClassInfo = methodInfo.getParent();
            // 后续修改 正确性有待考究
            curClassInfo = findClassInfoByRange(curClassInfoList, methodInfo.getBegin(), methodInfo.getEnd());
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
                        curMethodInfo.setMapped(true);
                        methodInfos.get(RelationShip.ADD.name()).add(curMethodInfo);
                        backtracking(curMethodInfo.getParent(), null);
                        return;
                    }
                    curMethodInfo.setTrackerInfo(RelationShip.SELF_CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                    curMethodInfo.getDiff().getJSONArray("data").add(oneDiff);
                    // 直接入库
                    methodUuidMap.put(curMethodInfo.getUuid(), trackerInfo.getRootUUID());
                    methodInfos.get(RelationShip.CHANGE.name()).add(curMethodInfo);
                    recordMapped(preMethodInfo, curMethodInfo);
                    preClassInfo = preMethodInfo.getParent();
                    curClassInfo = curMethodInfo.getParent();
                }
            }catch (NullPointerException e) {
                e.printStackTrace();
                log.error("method change declaration ：range:{}" , range );
            }
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(changeRelation)) {
            return;
        }
        if (curClassInfo != null) {
            backtracking(curClassInfo, preClassInfo);
            return;
        }
        log.error("method relation error,relation:{}" ,changeRelation);
    }

    /**
     *      重写
     * */
    private boolean isPackageDelete(PackageInfo packageInfo) {
        return false;
    }

    private FieldInfo findFieldInfoByRange(int begin, int end, List<FieldInfo> fieldInfos) {
        FieldInfo result = null;
        int r = -1;
        for (FieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.getBegin() == begin && fieldInfo.getEnd() == end) {
                return fieldInfo;
            }
            if (fieldInfo.getBegin() <= end && fieldInfo.getEnd() >= begin &&
                    (result == null ||  r >  (Math.abs(fieldInfo.getEnd() - end) +  Math.abs(fieldInfo.getBegin() - begin)))) {
                result = fieldInfo;
                r = Math.abs(result.getEnd() - end) +  Math.abs(result.getBegin() - begin);
            }
        }
        return result;
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
            if (trackerInfo == null) {
                log.error("handleMethod! tracker info is null");
                return methodInfo;
            }
            resetBaseInfo(methodInfo);
            methodUuidMap.put(methodInfo.getUuid(), trackerInfo.getRootUUID());
            methodInfo.setTrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID());
        }
        methodInfo.setMapped(true);
        methodInfos.get(relation).add(methodInfo);
        handleStatement(castBaseInfo(methodInfo.getChildren()), relation);
        return methodInfo;
    }

    private void handleStatement(List<StatementInfo> statementInfos, String relation) {
        if (statementInfos == null || statementInfos.isEmpty()) {
            return;
        }
        if (relation.equals(RelationShip.DELETE.name())) {
            for (StatementInfo statementInfo : statementInfos) {
                if (statementInfo == null) {
                    log.error("statementInfo is null");
                    continue;
                }
                resetBaseInfo(statementInfo);
                if (methodUuidMap.keySet().contains(statementInfo.getMethodUuid())) {
                    statementInfo.setMethodUuid(methodUuidMap.get(statementInfo.getMethodUuid()));
                }
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, statementInfo.getMethodUuid(), statementInfo.getBody());
                if (trackerInfo == null) {
                    log.error("handleStatement: tracker info is null");
                    return;
                }
                statementInfo.setTrackerInfo(relation, trackerInfo.getVersion(), trackerInfo.getRootUUID());
                this.statementInfos.get(relation).add(statementInfo);
                statementInfo.setMapped(true);
                handleStatement(castBaseInfo(statementInfo.getChildren()), relation);
            }
            return;
        }
        if (relation.equals(RelationShip.ADD.name())) {
            for (StatementInfo statementInfo : statementInfos) {
                statementInfo.setMapped(true);
                if (methodUuidMap.keySet().contains(statementInfo.getMethodUuid())) {
                    statementInfo.setMethodUuid(methodUuidMap.get(statementInfo.getMethodUuid()));
                }
                handleStatement(castBaseInfo(statementInfo.getChildren()), relation);
            }
            this.statementInfos.get(relation).addAll(statementInfos);
        }
    }

    private MethodInfo findMethodInfoByRange( List<MethodInfo> methodInfos ,int begin, int end) {
        for (MethodInfo methodInfo : methodInfos) {
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
            classInfo.setMapped(true);
            if (relation.equals(RelationShip.ADD.name())) {
                classInfos.get(relation).add(classInfo);
                methodInfos.get(relation).addAll(classInfo.getMethodInfos());
                for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                    methodInfo.setMapped(true);
                    handleStatement(castBaseInfo(methodInfo.getChildren()), relation);
                }
                fieldInfos.get(relation).addAll(classInfo.getFieldInfos());
                for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                    fieldInfo.setMapped(true);
                }
            } else if (relation.equals(RelationShip.DELETE.name())){
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                resetBaseInfo(classInfo);
                classInfo.setTrackerInfo( relation , trackerInfo.getVersion(), trackerInfo.getRootUUID());
                classInfos.get(relation).add(classInfo);

                for (MethodInfo methodInfo : classInfo.getMethodInfos()) {
                    trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                    methodUuidMap.put(methodInfo.getUuid(), trackerInfo.getRootUUID());
                    methodInfo.setTrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID());
                    resetBaseInfo(methodInfo);
                    methodInfos.get(relation).add(methodInfo);
                    handleStatement(castBaseInfo(methodInfo.getChildren()), relation);
                    methodInfo.setMapped(true);
                }

                for (FieldInfo fieldInfo : classInfo.getFieldInfos()) {
                    trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)fieldInfo.getParent()).getFilePath(), ((ClassInfo)fieldInfo.getParent()).getClassName(), fieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                    resetBaseInfo(fieldInfo);
                    fieldInfo.setTrackerInfo(relation , trackerInfo.getVersion(), trackerInfo.getRootUUID());
                    fieldInfo.setMapped(true);
                    fieldInfos.get(relation).add(fieldInfo);
                }
            }
        }
    }

    private ClassInfo findClassInfoByRange(List<ClassInfo> classInfos, int begin, int end) {
        if (classInfos == null || classInfos.isEmpty()) {
            return null;
        }

        if (classInfos.size() == 1) {
            return classInfos.get(0);
        }

        for (ClassInfo classInfo : classInfos) {
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

    private void analyzeModifiedField(DiffInfo.OneDiff oneDiff, List<FieldInfo> curFieldInfoList, List<FieldInfo> preFieldInfoList, List<ClassInfo> curClassInfoList, List<ClassInfo> preClassInfoList) {
        String changeRelation = oneDiff.getChangeRelation();
        // Change.Move 是比statement 更细粒度的语句的change
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            changeRelation = ChangeEntityDesc.StageIIOpt.OPT_CHANGE;
        }
        String range = oneDiff.getRange();
        FieldInfo preFieldInfo, curFieldInfo ;
        BaseInfo preClassInfo = null,curClassInfo = null;
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(changeRelation)) {
            int begin = rangeAnalyzeBegin(range);
            int end = rangeAnalyzeEnd(range);
            curFieldInfo = findFieldInfoByRange(begin, end, curFieldInfoList);
            if (baseInfoNullHandler(curFieldInfo, ProjectInfoLevel.FIELD.getName() + ExceptionMessage.CUR_INFO_NULL)) {
                return;
            }
            curFieldInfo.setMapped(true);
            fieldInfos.get(RelationShip.ADD.name()).add(curFieldInfo);
            curClassInfo = curFieldInfo.getParent();
            preClassInfo = findClassInfoByRange(preClassInfoList, begin, end);
        }
        // delete 情况处理不对
        if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(changeRelation)) {
            int begin = rangeAnalyzeBegin(range);
            int end = rangeAnalyzeEnd(range);
            preFieldInfo = findFieldInfoByRange(begin, end, preFieldInfoList);
            if (preFieldInfo == null) {
                log.error("analyzeModifiedField! preFieldInfo is null");
            }
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)preFieldInfo.getParent()).getFilePath(), ((ClassInfo)preFieldInfo.getParent()).getClassName(), preFieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                log.error("analyzeModifiedField! preFieldInfo is null");
                return;
            }
            resetBaseInfo(preFieldInfo);
            preFieldInfo.setTrackerInfo(RelationShip.DELETE.name(), trackerInfo.getVersion(), trackerInfo.getRootUUID());
            preFieldInfo.setMapped(true);
            fieldInfos.get(RelationShip.DELETE.name()).add(preFieldInfo);
            preClassInfo = preFieldInfo.getParent();
            curClassInfo = findClassInfoByRange(curClassInfoList, begin, end);
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(changeRelation)) {
            //before change
            int begin = rangeAnalyzeBegin(range.split("-")[0]);
            int end = rangeAnalyzeEnd(range.split("-")[0]);
            preFieldInfo = findFieldInfoByRange(begin, end, preFieldInfoList);
            begin = rangeAnalyzeBegin(range.split("-")[1]);
            end = rangeAnalyzeEnd(range.split("-")[1]);
            curFieldInfo = findFieldInfoByRange(begin, end, curFieldInfoList);
            if (baseInfoNullHandler(curFieldInfo, "analyzeModifiedField is null!" + changeRelation)) {
                return;
            }
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD, ((ClassInfo)preFieldInfo.getParent()).getFilePath(), ((ClassInfo)preFieldInfo.getParent()).getClassName(), preFieldInfo.getSimpleName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                log.error("analyzeModifiedField");
                return;
            }
            curFieldInfo.setTrackerInfo(RelationShip.SELF_CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
            fieldInfos.get(RelationShip.CHANGE.name()).add(curFieldInfo);
            curClassInfo = curFieldInfo.getParent();
            preClassInfo = preFieldInfo.getParent();
            preFieldInfo.setMapped(true);
            curFieldInfo.setMapped(true);
        }
        if (ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(changeRelation)) {
            return;
        }
        if (curClassInfo != null && preClassInfo != null) {
            backtracking(curClassInfo, preClassInfo);
            return;
        }
        log.error("method relation error,relation:{}" ,changeRelation);
    }


    private List<FieldInfo> getFieldInfoListByFileInfo(List<ClassInfo> classInfoList) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        Assert.notNull(classInfoList, "ERROR! analyzeModifiedField: ClassInfoList is null");
        for (ClassInfo classInfo : classInfoList) {
            fieldInfos.addAll(classInfo.getFieldInfos());
        }
        return fieldInfos;
    }

    private FileInfo findFileInfoByPath(RepoInfoBuilder curRepoInfo, String filePath) {
        for (FileInfo fileInfo : curRepoInfo.getFileInfos()) {
            if (fileInfo.getFilePath().contains(filePath) || filePath.contains(fileInfo.getFilePath())) {
                return fileInfo;
            }
        }
        return null;
    }

    private void analyzeModifiedStatement(DiffInfo.OneDiff oneDiff, List<StatementInfo> preStatementInfoList, List<StatementInfo> curStatementInfoList, List<MethodInfo> preMethodInfoList, List<MethodInfo> curMethodInfoList) {
        String changeRelation = oneDiff.getChangeRelation();
        String parentRange = oneDiff.getParentRange();
        final int level = -1;
        String range = oneDiff.getRange();
        // statement
        StatementInfo preStat, curStat;
        BaseInfo preParentStatement = null;
        BaseInfo curParentStatement = null;
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(changeRelation)) {
            int begin = rangeAnalyzeBegin(range);
            int end = rangeAnalyzeEnd(range);
            curStat = findStatementInfoByRange(curStatementInfoList, begin, end, level);
            if (curStat == null) {
                log.error("analyzeModifiedStatement OPT_INSERT statementInfo is null, range:{}", range);
                return;
            }
            backtrackingMethod(curStat);
            List<StatementInfo> addStat = new ArrayList<>(1);
            addStat.add(curStat);
            if (parentRange.length() != 0) {
                begin = rangeAnalyzeBegin(parentRange.split("-")[0]);
                end = rangeAnalyzeEnd(parentRange.split("-")[0]);
                preParentStatement = curStat.getLevel() > ProjectInfoLevel.STATEMENT.getLevel() ? findStatementInfoByRange(preStatementInfoList, begin, end, curStat.getLevel() - 1) : findMethodInfoByRange(preMethodInfoList, begin, end);
            }
            handleStatement(addStat, RelationShip.ADD.name());
            curParentStatement = curStat.getParent();
        }

        if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(changeRelation)) {
            int begin = rangeAnalyzeBegin(range);
            int end = rangeAnalyzeEnd(range);
            preStat = findStatementInfoByRange(preStatementInfoList, begin, end, level);
            backtrackingMethod(preStat);
            List<StatementInfo> deleteStat = new ArrayList<>(1);
            deleteStat.add(preStat);
            handleStatement(deleteStat, RelationShip.DELETE.name());
            String[] parentRanges = parentRange.split("-");
            if (parentRanges.length == 2 && parentRanges[1].length() > 4) {
                begin = rangeAnalyzeBegin(parentRange.split("-")[1]);
                end = rangeAnalyzeEnd(parentRange.split("-")[1]);
                if (preStat.getLevel() > ProjectInfoLevel.STATEMENT.getLevel()) {
                    curParentStatement = findStatementInfoByRange(curStatementInfoList, begin, end, preStat.getLevel() - 1);
                } else {
                    curParentStatement = findMethodInfoByRange(curMethodInfoList, begin, end);
                }
            }else {
                log.warn("analyzeModifiedStatement, OPT_DELETE, parent range:{}", parentRange);
                if (preStat.getLevel() > ProjectInfoLevel.STATEMENT.getLevel()) {
                    return;
                } else {
                    curParentStatement = preStat.getParent().getMappingNode();
                }
            }
            preParentStatement = preStat.getParent();
        }

        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(changeRelation) || ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(changeRelation) || ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE.equals(changeRelation)) {
            try {
                String[] ranges = range.split("-");
                if (ranges.length != 2  || ranges[0].length() < 5 || ranges[1].length() < 5) {
                    log.error("analyzeModifiedStatement, change range:{}", range);
                    return;
                }
                int begin = rangeAnalyzeBegin(ranges[1]);
                int end = rangeAnalyzeEnd(ranges[1]);
                curStat = findStatementInfoByRange(curStatementInfoList, begin, end, -1);
                if (curStat == null) {
                    log.error("change: curStat is null!");
                    return;
                }
                backtrackingMethod(curStat);
                begin = rangeAnalyzeBegin(ranges[0]);
                end = rangeAnalyzeEnd(ranges[0]);
                preStat = findStatementInfoByRange(preStatementInfoList, begin, end, curStat.getLevel());
                if (preStat == null) {
                    log.error("change: preStat is null!");
                    return;
                }
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, curStat.getMethodUuid(), preStat.getBody());
                if (trackerInfo == null) {
                    log.error("StatementInfo tracker info is null! method:{}", preStat.getMethodUuid());
                    return;
                }
                if (curStat.getLevel() != preStat.getLevel()) {
                    return;
                }
                curStat.setTrackerInfo(RelationShip.SELF_CHANGE.name(), trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                statementInfos.get(RelationShip.CHANGE.name()).add(curStat);
                curStat.setMapped(true);
                preStat.setMapped(true);
                preParentStatement = preStat.getParent();
                curParentStatement = curStat.getParent();
            }catch (ArrayIndexOutOfBoundsException e) {
                log.error(e.getMessage());
                log.error("statement range lack! range:{}", range);
                return;
            }
        }

        if (curParentStatement != null && preParentStatement != null) {
            backtracking(curParentStatement, preParentStatement);
        }
    }


    private void backtrackingMethod(StatementInfo statementInfo) {
        if (statementInfo == null) {
            log.error("backtrackingMethod: statementInfo is null!");
            return;
        }
        // 找到statement后，先回溯找到 meta_method 的uuid
        BaseInfo baseInfo = statementInfo.getParent();
        while(!(baseInfo instanceof MethodInfo)) {
            baseInfo = baseInfo.getParent();
        }
        backtracking(baseInfo, null);
        statementInfo.setMethodUuid(methodUuidMap.get(statementInfo.getMethodUuid()));
    }

    private StatementInfo findStatementInfoByRange(List<StatementInfo> statementInfoList, int begin, int end, int level) {
        StatementInfo result = null;
        int r  = -1;
        for (StatementInfo statementInfo : statementInfoList) {
            if ((level != -1 && statementInfo.getLevel() != level)) {
                continue;
            }
            // 先找父statement
            if (statementInfo.getBegin() == begin && statementInfo.getEnd() == end ) {
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

    private void backtracking(BaseInfo parent, BaseInfo preParent) {
        String change = RelationShip.CHANGE.name();
        if (parent instanceof ClassInfo) {
            ClassInfo classInfo = (ClassInfo) parent;
            if (classInfos.get(change).contains(classInfo)) {
                return;
            }
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS, classInfo.getFilePath(), classInfo.getClassName(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfo == null) {
                log.error("backtracking classInfo's tracker info is null! path: " + classInfo.getFilePath() + ",name: "+  classInfo.getClassName());
                return;
            }
            classInfo.setTrackerInfo(change, trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
            classInfos.get(change).add(classInfo);
            classInfo.setMapped(true);
        }

        if (parent instanceof MethodInfo) {
            MethodInfo methodInfo = (MethodInfo) parent;
            if (! methodInfos.get(change).contains(methodInfo)) {
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD, ((ClassInfo)methodInfo.getParent()).getFilePath(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
                if (trackerInfo == null) {
                    log.error("backtracking methodInfo's tracker info is null! path: " + ((ClassInfo)methodInfo.getParent()).getFilePath() + ",name: "+  methodInfo.getSignature());
                    return;
                }
                methodInfo.setTrackerInfo(change, trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                methodUuidMap.put(methodInfo.getUuid(), trackerInfo.getRootUUID());
                methodInfos.get(change).add(methodInfo);
                methodInfo.setMapped(true);
                backtracking(methodInfo.getParent(), null);
            }
            return;
        }

        if (parent instanceof StatementInfo) {
            if (parent.isMapped() || preParent.isMapped()) {
                return;
            }
            parent.setMapped(true);
            preParent.setMapped(true);
            StatementInfo statementInfo  = (StatementInfo) parent;
            StatementInfo preStatementInfo  = (StatementInfo) preParent ;
            if (! statementInfos.get(change).contains(statementInfo)) {
                if (methodUuidMap.keySet().contains(statementInfo.getMethodUuid())) {
                    statementInfo.setMethodUuid(methodUuidMap.get(statementInfo.getMethodUuid()));
                }
                TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT, statementInfo.getMethodUuid(), preStatementInfo.getBody());
                if (trackerInfo == null) {
                    log.error("backtracking statementInfo's tracker info is null!");
                    return;
                }
                statementInfo.setTrackerInfo(change, trackerInfo.getVersion() + 1, trackerInfo.getRootUUID());
                statementInfos.get(change).add(statementInfo);
                backtracking(statementInfo.getParent(), preStatementInfo.getParent());
            }
        }
    }

    private List<MethodInfo> getMethodInfoListByFileInfo(List<ClassInfo> classInfoList) {
        List<MethodInfo> methodInfoList = new ArrayList<>();
        Assert.notNull(classInfoList, "ERROR! analyzeModifiedMethod: ClassInfoList is null");
        for (ClassInfo classInfo : classInfoList) {
            methodInfoList.addAll(classInfo.getMethodInfos());
        }
        return methodInfoList;
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
                if (trackerInfoNullHandler(trackerInfo, ExceptionMessage.PACKAGE_TRACKER_INFO_NULL)) {
                    continue;
                }
                modifyPackageUuid.put(hashCode, trackerInfo);
                packageInfo.setTrackerInfo(new TrackerInfo(change , trackerInfo.getVersion(), trackerInfo.getRootUUID()));
                packageInfos.get(change).add(packageInfo);
            }
        }

        for (FileInfo fileInfo : curRepoInfo.getFileInfos()) {
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FILE, fileInfo.getFilePath(), curRepoInfo.getRepoUuid(), curRepoInfo.getBranch());
            if (trackerInfoNullHandler(trackerInfo, ExceptionMessage.FILE_TRACKER_INFO_NULL + "file path: " + fileInfo.getFilePath())){
                continue;
            }
            fileInfo.setTrackerInfo(new TrackerInfo(RelationShip.CHANGE.name() , trackerInfo.getVersion() + 1, trackerInfo.getRootUUID()));
            fileInfos.get(change).add(fileInfo);
        }
    }

    /**
     * 处理 class、method、field、statement是delete的情况
     *  commit committer commitDate commitMessage 都为当前版本的信息
     */
    private void resetBaseInfo(BaseInfo baseInfo) {
        if (baseInfo == null) {
            log.error("resetBaseInfo");
            return;
        }
        baseInfo.setCommit(curRepoInfo.getCommit());
        baseInfo.setCommitter(curRepoInfo.getCommitter());
        baseInfo.setCommitDate(curRepoInfo.getCommitDate());
        baseInfo.setCommitMessage(curRepoInfo.getCommitMessage());
    }

    private void recordMapped(BaseInfo preBaseInfo, MethodInfo curBaseInfo) {
        preBaseInfo.setMapped(true);
        preBaseInfo.setMappingNode(curBaseInfo);
        curBaseInfo.setMapped(true);
        curBaseInfo.setMappingNode(preBaseInfo);
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

    private boolean baseInfoNullHandler(BaseInfo baseInfo, String message) {
        if (baseInfo == null) {
            log.error(message);
        }
        return baseInfo == null;
    }

    private boolean trackerInfoNullHandler(TrackerInfo trackerInfo, String message) {
        if (trackerInfo == null) {
            log.error(message);
        }
        return trackerInfo == null;
    }

}