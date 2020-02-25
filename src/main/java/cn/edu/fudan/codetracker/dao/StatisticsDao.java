/**
 * @description:
 * @author: fancying
 * @create: 2019-11-12 09:59
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class StatisticsDao {
    private StatisticsMapper statisticsMapper;
    private Map<String,List<Long>> committerMap;

    @Autowired
    public void setStatisticsMapper(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    public List<VersionStatistics> getStatisticsByType(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMethodStatistics(repoUuid, branch);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getClassStatistics(repoUuid, branch);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getFileStatistics(repoUuid, branch);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.getPackageStatistics(repoUuid, branch);
        }
        return null;
    }

    /**
     * most modified
     */
    public List<MostModifiedInfo> getMostModifiedInfo(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMostModifiedMethod(repoUuid, branch);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getMostModifiedClass(repoUuid, branch);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getMostModifiedFile(repoUuid, branch);
        }
        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostModifiedPackage(repoUuid,branch);
        }
        return null;
    }

    /**
     * modification of most developers participate in
     */
    public List<MostDevelopersInfo> getMostDevelopersInvolved(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedMethod(repoUuid, branch);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedClass(repoUuid, branch);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedFile(repoUuid, branch);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedPackage(repoUuid, branch);
        }
        return null;
    }

    /**
     * most modified in given time
     */
    public List<MostDevelopersInfo> getMostModifiedByTime(String repoUuid, String branch, String type,String beginDate, String endDate){
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMostModifiedMethodByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getMostModifiedClassByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getMostModifiedFileByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostModifiedPackageByTime(repoUuid,branch, beginDate, endDate);
        }
        return null;
    }

    /**
     * get most modified methods info in given package
     */
    public List<MostModifiedMethod> getMostModifiedMethodByPackage(String repoUuid, String packageUuid, String branch){
        return statisticsMapper.getMostModifiedMethodByPackage(repoUuid, packageUuid, branch);
    }

    /**
     * developer most focus on in given time
     */
    public List<DeveloperMostFocus> getDeveloperFocusMost(String type, String committer, String beginDate, String endDate){
        type = type.toUpperCase();
        List<DeveloperMostFocus> developerMostFocusList = new ArrayList<>();
        List<DeveloperMostFocus> temp;
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            temp = statisticsMapper.methodDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                List<String> content = statisticsMapper.getContentByMethodId(dmf.getUuid(),committer,beginDate,endDate);
                dmf.setContent(content);
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            temp = statisticsMapper.classDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            temp = statisticsMapper.fileDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            temp = statisticsMapper.packageDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
        }
        return null;
    }

    /**
     * get commit info by uuid
     */
    public List<CommitTimeLine> getCommitTimeLine(String type, String uuid){
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.methodCommitTimeLine(uuid);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.classCommitTimeLine(uuid);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.fileCommitTimeLine(uuid);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.packageCommitTimeLine(uuid);
        }
        return null;
    }


    /**
     * get commit info by committer
     */
    public List<CommitterHistory> getCommitInfoByCommitter(String committer){
        List<CommitInfoByCommitter> commitList = statisticsMapper.getCommitInfoByCommitter(committer);
        List<CommitterHistory> historyList = new ArrayList<>();
        for (CommitInfoByCommitter cInfo: commitList) {
            List<BasicInfoByCommitId> fileInfo = new ArrayList<>();
            List<BasicInfoByCommitId> methodInfo = new ArrayList<>();
            fileInfo = statisticsMapper.getFileInfoByCommitId(cInfo.getCommitId());
            methodInfo = statisticsMapper.getMethodInfoByCommitId(cInfo.getCommitId());
            CommitterHistory cHistory = new CommitterHistory();
            cHistory.setCommitId(cInfo.getCommitId());
            cHistory.setCommitDate(cInfo.getCommitDate());
            cHistory.setCommitMessage(cInfo.getCommitMessage());
            cHistory.setFileList(fileInfo);
            cHistory.setMethodList(methodInfo);
            historyList.add(cHistory);
        }
        return historyList;
    }


    /**
     * get delete statement former info by committer in given time
     */
    public List<DeleteStatementInfo> getDeleteStatementFormerInfoByCommitter(String committer, String repoUuid, String branch, String beginDate, String endDate){
        List<DeleteStatementInfo> statementUuidList = statisticsMapper.getDeleteStatementUuidList(committer, repoUuid, branch, beginDate, endDate);
        List<DeleteStatementInfo> deleteStatementInfoList = new ArrayList<>();
        for (DeleteStatementInfo deleteStatementInfo : statementUuidList) {
            DeleteStatementInfo deleteStatementInfo1 = statisticsMapper.getDeleteStatementFirstInfo(deleteStatementInfo.getStatementUuid());
            DeleteStatementInfo deleteStatementInfo2 = statisticsMapper.getDeleteStatementLastInfo(deleteStatementInfo.getStatementUuid());
            deleteStatementInfo.setFirstCommitter(deleteStatementInfo1.getFirstCommitter());
            deleteStatementInfo.setFirstCommitDate(deleteStatementInfo1.getFirstCommitDate());
            deleteStatementInfo.setLastCommitter(deleteStatementInfo2.getLastCommitter());
            deleteStatementInfo.setLastCommitDate(deleteStatementInfo2.getLastCommitDate());
            deleteStatementInfo.setBody(deleteStatementInfo2.getBody());
            deleteStatementInfoList.add(deleteStatementInfo);
        }
        return deleteStatementInfoList;
    }


    /**
     * get statement info by method and committer in given time
     */
    public List<StatementInfoByMethod> getStatementInfoByMethod(String committer, String methodUuid, String beginDate, String endDate){
        return statisticsMapper.getStatementInfoByMethod(committer, methodUuid, beginDate, endDate);
    }


    /**
     * get change committer
     */
    public String getChangeCommitter(String type, String beginDate, String... args) {
        if (beginDate == null) {
            switch (type) {
                case "class":
                    return statisticsMapper.getChangeCommitterByClass(args[0], args[1], args[2], args[3], args[4]);
                case "method":
                    return statisticsMapper.getChangeCommitterByMethod(args[0], args[1], args[2], args[3], args[4], args[5]);
                case "field":
                    return statisticsMapper.getChangeCommitterByField(args[0], args[1], args[2], args[3], args[4], args[5]);
                case "statement":
                    return statisticsMapper.getChangeCommitterByStatement(args[0], args[1], args[2]);
                default:
                    return "";
            }
        } else {
            switch (type) {
                case "class":
                    return statisticsMapper.getChangeCommitterByClassDate(args[0], args[1], args[2], args[3], args[4], beginDate);
                case "method":
                    return statisticsMapper.getChangeCommitterByMethodDate(args[0], args[1], args[2], args[3], args[4], args[5], beginDate);
                case "field":
                    return statisticsMapper.getChangeCommitterByFieldDate(args[0], args[1], args[2], args[3], args[4], args[5], beginDate);
                case "statement":
                    return statisticsMapper.getChangeCommitterByStatementDate(args[0], args[1], args[2], beginDate);
                default:
                    return "";
            }
        }

    }

    /**
     * get valid line info
     */
    public List<ValidLineInfo> getValidLineInfo(String type, String repoUuid, String branch, String beginDate, String endDate) {
        switch (type) {
            case "class":
                return statisticsMapper.getValidLineInfoByClass(repoUuid, branch, beginDate, endDate);
            case "method":
                return statisticsMapper.getValidLineInfoByMethod(repoUuid, branch, beginDate, endDate);
            case "field":
                return statisticsMapper.getValidLineInfoByField(repoUuid, branch, beginDate, endDate);
            case "statement":
                return statisticsMapper.getValidLineInfoByStatement(repoUuid, branch, beginDate, endDate);
            default:
                return null;
        }
    }

    /**
     * get committer line info by commit
     */
    public List<CommitterLineInfo> getCommitterLineInfo(String repoUuid, String branch, String commitDate) {
        return statisticsMapper.getCommitterLineInfo(repoUuid, branch, commitDate);
    }


    public String getMetaMethodUuidByMethod(String beginDate, String filePath, String repoUuid, String branch, String className, String signature, String commitDate) {
        if (beginDate == null) {
            return statisticsMapper.getMetaMethodUuidByMethod(filePath, repoUuid, branch, className, signature, commitDate);
        } else {
            return statisticsMapper.getMetaMethodUuidByMethodDate(filePath, repoUuid, branch, className, signature, commitDate, beginDate);
        }

    }


    /**
     * 临时接口
     */
    public List<TempMostInfo> getFocus(String committer, String beginDate, String endDate, String repoUuid, String branch) {
        List<MostModifiedInfo> packageInfos = statisticsMapper.getPackageInfoMost(committer, beginDate, endDate, repoUuid, branch);
        List<TempMostInfo> packageList = new ArrayList<>();
        for (MostModifiedInfo mostModifiedInfo: packageInfos) {
            TempMostInfo packageInfo = new TempMostInfo();
            packageInfo.setName(mostModifiedInfo.getPackageName());
            packageInfo.setQuantity(mostModifiedInfo.getVersion());
            packageInfo.setUuid(mostModifiedInfo.getUuid());
            List<MostModifiedInfo> classInfos = statisticsMapper.getClassInfoMost(committer,mostModifiedInfo.getModuleName(),mostModifiedInfo.getPackageName(),beginDate,endDate,repoUuid,branch);
            List<TempMostInfo> classList = new ArrayList<>();
            for (MostModifiedInfo modifiedInfo : classInfos) {
                TempMostInfo classInfo = new TempMostInfo();
                classInfo.setName(modifiedInfo.getClassName());
                classInfo.setQuantity(modifiedInfo.getVersion());
                classInfo.setUuid(modifiedInfo.getUuid());
                List<MostModifiedInfo> methodInfos = statisticsMapper.getMethodInfoMost(committer,modifiedInfo.getFilePath(),modifiedInfo.getClassName(),beginDate,endDate,repoUuid,branch);
                List<TempMostInfo> methodList = new ArrayList<>();
                for (MostModifiedInfo methodInfo : methodInfos) {
                    TempMostInfo method = new TempMostInfo();
                    method.setName(methodInfo.getMethodName());
                    method.setQuantity(methodInfo.getVersion());
                    method.setChildInfos(null);
                    method.setUuid(methodInfo.getUuid());
                    methodList.add(method);
                }
                classInfo.setChildInfos(methodList);
                classList.add(classInfo);
            }
            packageInfo.setChildInfos(classList);
            packageList.add(packageInfo);
        }
        return packageList;
    }


    /**
     * 临时接口
     */
    public List<MethodHistory> getMethodHistory(String methodUuid) {
        return statisticsMapper.getMethodHistory(methodUuid);
    }

    /**
     * 一次性获取全部可选语句
     */
    public List<Map<String,String>> getAllValidStatement(String methodUuid, String commitDate, String body) {
        String[] strs = body.split("\\n");
        List<Map<String,String>> mapList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        List<StatementInfoByMethod> statementInfoByMethodList = statisticsMapper.getAllValidStatement(methodUuid, commitDate);
        String lastStatementUuid = "";
        for (StatementInfoByMethod statementInfoByMethod : statementInfoByMethodList) {
            if (!statementInfoByMethod.getStatementUuid().equals(lastStatementUuid) && !"DELETE".equals(statementInfoByMethod.getChangeRelation())) {
                list.add(statementInfoByMethod.getBody());
                lastStatementUuid = statementInfoByMethod.getStatementUuid();
            }
        }

        for (String str : strs) {
            boolean find = false;
            String tmp = str.trim();
            Map<String,String> map = new HashMap<>();
            if ("".equals(tmp)) {
                map.put(str, null);
            } else {
                for (String s : list) {
                    if (s.startsWith(tmp) || tmp.startsWith(s)) {
                        map.put(str, s);
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    map.put(str, null);
                }
            }
            mapList.add(map);
        }
        return mapList;
    }


    /**
     * 统计存活周期
     */
    public Map<String,List<Long>> getSurviveStatementStatistics(String beginDate, String endDate, String repoUuid, String branch) {
        List<SurviveStatementInfo> surviveStatementInfos = statisticsMapper.getSurviveStatement(beginDate, endDate, repoUuid, branch);
        committerMap = new HashMap<>();
        SurviveStatementInfo lastSurviveStatement = null;
        for (SurviveStatementInfo surviveStatementInfo : surviveStatementInfos) {
            if (lastSurviveStatement != null) {
                if (surviveStatementInfo.getStatementUuid() != lastSurviveStatement.getStatementUuid()) {
                    if (lastSurviveStatement.getChangeRelation().equals("ADD") || lastSurviveStatement.getChangeRelation().equals("SELF_CHANGE")) {
                        long days = calBetweenDays(lastSurviveStatement.getCommitDate(), endDate);
                        if (days > 0) {
                            saveInMap(lastSurviveStatement.getCommitter(), days);
                        }
                    }
                } else {
                    if (lastSurviveStatement.getChangeRelation().equals("ADD") || lastSurviveStatement.getChangeRelation().equals("SELF_CHANGE")) {
                        if (surviveStatementInfo.getChangeRelation().equals("SELF_CHANGE") || surviveStatementInfo.getChangeRelation().equals("DELETE")) {
                            long days = calBetweenDays(lastSurviveStatement.getCommitDate(), surviveStatementInfo.getCommitDate());
                            if (days > 0) {
                                saveInMap(lastSurviveStatement.getCommitter(), days);
                            }
                        }
                    }
                }
            }
            lastSurviveStatement = surviveStatementInfo;
        }
        return committerMap;
    }

    private void saveInMap(String name, long days) {
        List<Long> list = new ArrayList<>();
        if (committerMap.keySet().contains(name)) {
            list = committerMap.get(name);
            list.add(days);
            committerMap.replace(name, list);
        } else {
            list.add(days);
            committerMap.put(name, list);
        }
    }

    private long calBetweenDays(String beginStr, String endStr) {
        final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date begin = FORMATTER.parse(beginStr);
            Date end = FORMATTER.parse(endStr);
            long between = (end.getTime() - begin.getTime()) / (1000L*3600L*24L);
            return between;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取语句历史切片
     */
    public List<SurviveStatementInfo> getStatementHistory(String methodUuid, String body) {
        return statisticsMapper.getStatementHistory(methodUuid, body);
    }

    /**
     * 删除操作
     */
    public void delete(String repoUuid, String branch) {
        statisticsMapper.deleteFromMetaPackage(repoUuid, branch);
        statisticsMapper.deleteFromMetaFile(repoUuid, branch);
        statisticsMapper.deleteFromMetaClass(repoUuid, branch);
        statisticsMapper.deleteFromMetaMethod(repoUuid, branch);
        statisticsMapper.deleteFromMetaField(repoUuid, branch);
        statisticsMapper.deleteFromMetaStatement(repoUuid, branch);
        statisticsMapper.deleteFromRawPackage(repoUuid, branch);
        statisticsMapper.deleteFromRawFile(repoUuid, branch);
        statisticsMapper.deleteFromRawClass(repoUuid, branch);
        statisticsMapper.deleteFromRawMethod(repoUuid, branch);
        statisticsMapper.deleteFromRawField(repoUuid, branch);
        statisticsMapper.deleteFromRawStatement(repoUuid, branch);
        statisticsMapper.deleteFromRelationStatement(repoUuid, branch);
        statisticsMapper.deleteFromLineCount(repoUuid, branch);
        statisticsMapper.deleteFromTrackerRepo(repoUuid, branch);
    }

}