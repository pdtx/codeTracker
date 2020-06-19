/**
 * @description:
 * @author: fancying
 * @create: 2019-11-12 09:59
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class StatisticsDao implements PublicConstants {
    private StatisticsMapper statisticsMapper;
    private Map<String,List<Long>> committerMap;

    @Autowired
    public void setStatisticsMapper(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    /**
     * get valid line info
     */
    public List<ValidLineInfo> getValidLineInfo(String type, String repoUuid, String beginDate, String endDate) {
        switch (type) {
            case CLASS:
                return statisticsMapper.getValidLineInfoByClass(repoUuid, beginDate, endDate);
            case METHOD:
                return statisticsMapper.getValidLineInfoByMethod(repoUuid, beginDate, endDate);
            case FIELD:
                return statisticsMapper.getValidLineInfoByField(repoUuid, beginDate, endDate);
            case STATEMENT:
                return statisticsMapper.getValidLineInfoByStatement(repoUuid, beginDate, endDate);
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
        List<SurviveStatementInfo> listClass = statisticsMapper.getSurviveClass(beginDate, endDate, repoUuid, branch);
        if (listStatement != null && listStatement.size() != 0) {
            surviveStatementInfos.addAll(listStatement);
        }
        if (listMethod != null && listMethod.size() != 0) {
            surviveStatementInfos.addAll(listMethod);
        }
        if (listField != null && listField.size() != 0) {
            surviveStatementInfos.addAll(listField);
        }
        if (listClass != null && listClass.size() != 0) {
            surviveStatementInfos.addAll(listClass);
        }
        committerMap = new HashMap<>();
        SurviveStatementInfo lastSurviveStatement = null;
        for (SurviveStatementInfo surviveStatementInfo : surviveStatementInfos) {
            if (lastSurviveStatement != null) {
                if (!lastSurviveStatement.getStatementUuid().equals(surviveStatementInfo.getStatementUuid())) {
                    if (lastSurviveStatement.getChangeRelation().equals(ADD) || lastSurviveStatement.getChangeRelation().equals(SELF_CHANGE)) {
                        long days = calBetweenDays(lastSurviveStatement.getCommitDate(), endDate);
                        if (days > 0) {
                            saveInMap(lastSurviveStatement.getCommitter(), days);
                        }
                    }
                } else {
                    if (lastSurviveStatement.getChangeRelation().equals(ADD) || lastSurviveStatement.getChangeRelation().equals(SELF_CHANGE)) {
                        if (surviveStatementInfo.getChangeRelation().equals(SELF_CHANGE) || surviveStatementInfo.getChangeRelation().equals(DELETE)) {
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
        if (lastSurviveStatement != null && !lastSurviveStatement.getChangeRelation().equals(DELETE)) {
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


    public Map<String,List<Long>> getDeleteInfo(String beginDate,String endDate,String repoUuid) {
        List<SurviveStatementInfo> list = statisticsMapper.getDeleteInfo(beginDate, endDate, repoUuid);
        Map<String,List<Long>> map = new HashMap<>();
        SurviveStatementInfo lastStat = null;
        for (SurviveStatementInfo stat: list) {
            if (DELETE.equals(stat.getChangeRelation())) {
                lastStat = stat;
            } else if (ADD.equals(stat.getChangeRelation())) {
                if (lastStat != null && lastStat.getStatementUuid().equals(stat.getStatementUuid())) {
                    long between = calBetweenDays(stat.getCommitDate(),lastStat.getCommitDate());
                    if (map.keySet().contains(lastStat.getCommitter())) {
                        map.get(lastStat.getCommitter()).add(between);
                    } else {
                        List<Long> timeList = new ArrayList<>();
                        timeList.add(between);
                        map.put(lastStat.getCommitter(),timeList);
                    }
                }
            }
        }
        return map;
    }


    public Map<String,List<Long>> getChangeInfo(String repoUuid, String beginDate, String endDate) {
        List<SurviveStatementInfo> list = statisticsMapper.getChangeInfo(beginDate,endDate,repoUuid);
        Map<String,List<Long>> map = new HashMap<>();
        SurviveStatementInfo lastStat = null;
        for (SurviveStatementInfo stat: list) {
            if((lastStat == null) || (lastStat != null && !stat.getStatementUuid().equals(lastStat.getStatementUuid()))) {
                if (!SELF_CHANGE.equals(stat.getChangeRelation())) {
                    continue;
                } else {
                    lastStat = stat;
                }
            } else if (lastStat != null && stat.getStatementUuid().equals(lastStat.getStatementUuid())) {
                if (ADD.equals(stat.getChangeRelation()) || SELF_CHANGE.equals(stat.getChangeRelation())) {
                    long res = calBetweenDaysWithRange(stat.getCommitDate(),lastStat.getCommitDate(),beginDate,endDate);
                    if (res != -1L) {
                        if (map.keySet().contains(lastStat.getCommitter())) {
                            map.get(lastStat.getCommitter()).add(res);
                        } else {
                            List<Long> nums = new ArrayList<>();
                            nums.add(res);
                            map.put(lastStat.getCommitter(),nums);
                        }
                    }
                    if (ADD.equals(stat.getChangeRelation())) {
                        lastStat = null;
                    } else {
                        lastStat = stat;
                    }
                }
            }
        }
        return map;
    }

    private long calBetweenDaysWithRange(String beginStr, String endStr, String beginDate, String endDate) {
        final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date from = FORMATTER.parse(beginDate);
            Date to = FORMATTER.parse(endDate);
            Date end = FORMATTER.parse(endStr);
            if (end.getTime() > to.getTime() || end.getTime() < from.getTime()) {
                return -1L;
            }
            Date begin = FORMATTER.parse(beginStr);
            Double between = (end.getTime() - begin.getTime()) / (1000.0*3600.0*24.0);
            long betweenLong = (Math.round(between));
            return betweenLong;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1L;
        }
    }

    public List<TempMostInfo> getFocusFiles(String repoUuid, String beginDate, String endDate) {
        return statisticsMapper.getFocusFileNum(repoUuid, beginDate, endDate);
    }



}