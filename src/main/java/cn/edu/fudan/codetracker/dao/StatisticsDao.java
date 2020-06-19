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
     * 统计存活周期
     */
    public Map<String,List<Long>> getSurviveStatementStatistics(String beginDate, String endDate, String repoUuid, String branch) {
        List<SurviveStatementInfo> surviveStatementInfos = new ArrayList<>();
        List<SurviveStatementInfo> listStatement = statisticsMapper.getSurviveStatement(beginDate, endDate, repoUuid, branch);
        List<SurviveStatementInfo> listMethod = statisticsMapper.getSurviveMethod(beginDate, endDate, repoUuid, branch);
        List<SurviveStatementInfo> listField = statisticsMapper.getSurviveField(beginDate, endDate, repoUuid, branch);
        if (listStatement != null && listStatement.size() != 0) {
            surviveStatementInfos.addAll(listStatement);
        }
        if (listMethod != null && listMethod.size() != 0) {
            surviveStatementInfos.addAll(listMethod);
        }
        if (listField != null && listField.size() != 0) {
            surviveStatementInfos.addAll(listField);
        }
        committerMap = new HashMap<>();
        SurviveStatementInfo lastSurviveStatement = null;
        for (SurviveStatementInfo surviveStatementInfo : surviveStatementInfos) {
            if (lastSurviveStatement != null) {
                if (!lastSurviveStatement.getStatementUuid().equals(surviveStatementInfo.getStatementUuid())) {
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
        if (lastSurviveStatement != null && !lastSurviveStatement.getChangeRelation().equals("DELETE")) {
            long days = calBetweenDays(lastSurviveStatement.getCommitDate(), endDate);
            if (days > 0) {
                saveInMap(lastSurviveStatement.getCommitter(), days);
            }
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
            Double between = (end.getTime() - begin.getTime()) / (1000.0*3600.0*24.0);
            long betweenLong = (Math.round(between));
            return betweenLong;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
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
        statisticsMapper.deleteFromTrackerRepo(repoUuid, branch);
    }


}