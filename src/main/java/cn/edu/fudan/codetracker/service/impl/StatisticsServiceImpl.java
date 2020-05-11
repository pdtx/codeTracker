/**
 * @description:
 * @author: fancying
 * @create: 2019-11-11 21:28
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.StatisticsService;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private StatisticsDao statisticsDao;
    private Map<String,Integer> committerMap;
    private SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final int addOne = 1;
    private final int addTwo = 2;


    @Override
    public Map<String,Integer> getValidLineCount(String repoUuid, String branch, String beginDate, String endDate) {
        Map<String,Integer> map = new TreeMap<>();
        List<ValidLineInfo> list = new ArrayList<>();
        list.addAll(statisticsDao.getValidLineInfo("class", repoUuid, branch, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo("method", repoUuid, branch, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo("field", repoUuid, branch, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo("statement", repoUuid, branch, beginDate, endDate));
        String lastMetaUuid = "";
        for (ValidLineInfo validInfo: list) {
            if (validInfo.getMetaUuid().equals(lastMetaUuid)) {
                continue;
            }
            if (validInfo.getChangeRelation().equals("DELETE")) {
                lastMetaUuid = validInfo.getMetaUuid();
                continue;
            }
            lastMetaUuid = validInfo.getMetaUuid();
            if (map.keySet().contains(validInfo.getCommitter())) {
                map.replace(validInfo.getCommitter(), map.get(validInfo.getCommitter())+1);
            } else {
                map.put(validInfo.getCommitter(), 1);
            }
        }
        return map;
    }



    @Override
    public Map<String,Map<String,Double>> getSurviveStatementStatistics(String beginDate, String endDate, String repoUuid, String branch) {
        Map<String,Map<String,Double>> map = new HashMap<>();
        Map<String,List<Long>> temp = statisticsDao.getSurviveStatementStatistics(beginDate, endDate, repoUuid, branch);
        for (String key : temp.keySet()) {
            List<Long> list = temp.get(key);
            list.sort(Comparator.comparingLong(Long::longValue));
            Map<String,Double> newMap = new HashMap<>();
            newMap.put("min",(double)list.get(0));
            newMap.put("max",(double)list.get(list.size()-1));
            if (list.size()%2 == 0) {
                Double median = (list.get(list.size()/2) + list.get((list.size()/2)-1)) / 2.0;
                newMap.put("median", new BigDecimal(median).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            } else {
                newMap.put("median",(double)list.get((list.size()-1)/2));
            }
            Long sum = 0L;
            for (Long l : list) {
                sum += l;
            }
            Double average = sum / (list.size() * 1.0);
            newMap.put("average",new BigDecimal(average).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            map.put(key,newMap);
        }
        return map;
    }


    @Override
    public void delete(String repoUuid, String branch) {
        statisticsDao.delete(repoUuid, branch);
    }

    /**
     * getter and setter
     * */
    @Autowired
    public void setStatisticsDao(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }





    //未用到
    @Override
    public List<VersionStatistics> getStatistics(String repoUuid, String branch, String type) {
        return statisticsDao.getStatisticsByType(repoUuid, branch, type);
    }

    @Override
    public List<MostModifiedInfo> getMostModifiedInfo(String repoUuid, String branch, String type) {
        return statisticsDao.getMostModifiedInfo(repoUuid, branch, type);
    }

    @Override
    public List<MostDevelopersInfo> getMostDevelopersInvolved(String repoUuid, String branch, String type) {
        return statisticsDao.getMostDevelopersInvolved(repoUuid, branch, type);
    }

    @Override
    public List<MostDevelopersInfo> getMostModifiedByTime(String repoUuid, String branch, String type, String beginDate, String endDate){
        return statisticsDao.getMostModifiedByTime(repoUuid, branch, type, beginDate, endDate);
    }

    @Override
    public List<MostModifiedMethod> getMostModifiedMethodByPackage(String repoUuid, String packageUuid, String branch){
        return statisticsDao.getMostModifiedMethodByPackage(repoUuid, packageUuid, branch);
    }

    @Override
    public List<DeveloperMostFocus> getDeveloperFocusMost(String type, String committer,String beginDate, String endDate){
        return statisticsDao.getDeveloperFocusMost(type, committer, beginDate, endDate);
    }

    @Override
    public List<CommitTimeLine> getCommitTimeLine(String type, String uuid){
        return statisticsDao.getCommitTimeLine(type, uuid);
    }

    @Override
    public List<CommitterHistory> getCommitHistoryByCommitter(String committer){
        return statisticsDao.getCommitInfoByCommitter(committer);
    }

    @Override
    public List<DeleteStatementInfo> getDeleteStatementFormerInfoByCommitter(String committer, String repoUuid, String branch, String beginDate, String endDate){
        return statisticsDao.getDeleteStatementFormerInfoByCommitter(committer, repoUuid, branch, beginDate, endDate);
    }

    @Override
    public List<StatementInfoByMethod> getStatementInfoByMethod(String committer, String methodUuid, String beginDate, String endDate){
        return statisticsDao.getStatementInfoByMethod(committer, methodUuid, beginDate, endDate);
    }


//    @Override
//    public Map<String,Integer> getChangeCommitterInfo(String repoUuid, String commit, String repoPath, String branch) {
//        calculateCommitterRemainLine(repoUuid, commit, repoPath, branch, null);
//        Map<String,Integer> sortMap = new TreeMap<String, Integer>();
//        sortMap.putAll(committerMap);
//        return sortMap;
//    }
//
//    @Override
//    public Map<String,Integer> getChangeCommitterInfoByDate(String repoUuid, String commit, String repoPath, String branch, String beginDate) {
//        calculateCommitterRemainLine(repoUuid, commit, repoPath, branch, beginDate);
//        Map<String,Integer> sortMap = new TreeMap<String, Integer>();
//        sortMap.putAll(committerMap);
//        return sortMap;
//    }

//    private void calculateCommitterRemainLine(String repoUuid, String commit, String repoPath, String branch, String beginDate) {
//        committerMap = new HashMap<>();
//        JGitHelper jGitHelper = new JGitHelper(repoPath);
//        jGitHelper.checkout(commit);
//        RepoInfoBuilder repoInfo = new RepoInfoBuilder(repoUuid, commit, repoPath, jGitHelper, branch, null, null);
//        List<ClassInfo> classInfoList = repoInfo.getClassInfos();
//        List<MethodInfo> methodInfoList = repoInfo.getMethodInfos();
//        List<FieldInfo> fieldInfoList = repoInfo.getFieldInfos();
//        List<StatementInfo> statementInfoList = new ArrayList<>();
//        for (ClassInfo classInfo: classInfoList) {
//            String classCommitter = statisticsDao.getChangeCommitter("class", beginDate, classInfo.getFilePath(), classInfo.getRepoUuid(), classInfo.getBranch(), classInfo.getClassName(), FORMATTER.format(classInfo.getCommitDate()));
//            if (classCommitter == null) {
//                continue;
//            }
//            if (committerMap.keySet().contains(classCommitter)) {
//                committerMap.replace(classCommitter, committerMap.get(classCommitter) + addTwo);
//            } else {
//                committerMap.put(classCommitter, addTwo);
//            }
//        }
//        for(MethodInfo methodInfo: methodInfoList) {
//            if (methodInfo.getChildren() != null && methodInfo.getChildren().size() > 0) {
//                for(StatementInfo statementInfo: (List<StatementInfo>)methodInfo.getChildren()) {
//                    String methodUuid = statisticsDao.getMetaMethodUuidByMethod(beginDate, ((ClassInfo)methodInfo.getParent()).getFilePath(), methodInfo.getRepoUuid(), methodInfo.getBranch(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), FORMATTER.format(methodInfo.getCommitDate()));
//                    statementInfo.setMethodUuid(methodUuid);
//                    statementInfoList.add(statementInfo);
//                }
//            }
//            String methodCommitter = statisticsDao.getChangeCommitter("method", beginDate, ((ClassInfo)methodInfo.getParent()).getFilePath(), methodInfo.getRepoUuid(), methodInfo.getBranch(), ((ClassInfo)methodInfo.getParent()).getClassName(), methodInfo.getSignature(), FORMATTER.format(methodInfo.getCommitDate()));
//            if (methodCommitter == null) {
//                continue;
//            }
//            int add = addTwo;
//            if (methodInfo.getChildren() == null || methodInfo.getChildren().size() == 0) {
//                add = addOne;
//            }
//            if (committerMap.keySet().contains(methodCommitter)) {
//                committerMap.replace(methodCommitter, committerMap.get(methodCommitter) + add);
//            } else {
//                committerMap.put(methodCommitter, add);
//            }
//        }
//        for(FieldInfo fieldInfo: fieldInfoList) {
//            String fieldCommitter = statisticsDao.getChangeCommitter("field", beginDate, ((ClassInfo)fieldInfo.getParent()).getFilePath(), fieldInfo.getRepoUuid(), fieldInfo.getBranch(), ((ClassInfo)fieldInfo.getParent()).getClassName(), fieldInfo.getSimpleName(), FORMATTER.format(fieldInfo.getCommitDate()));
//            if (fieldCommitter == null) {
//                continue;
//            }
//            if (committerMap.keySet().contains(fieldCommitter)) {
//                committerMap.replace(fieldCommitter, committerMap.get(fieldCommitter) + addOne);
//            } else {
//                committerMap.put(fieldCommitter, addOne);
//            }
//        }
//
//        while (statementInfoList != null && statementInfoList.size() > 0) {
//            statementInfoList = getChildList(statementInfoList, beginDate);
//        }
//    }

    private List<StatementInfo> getChildList(List<StatementInfo> statementInfoList, String beginDate) {
        List<StatementInfo> statementInfos = new ArrayList<>();
        for(StatementInfo statementInfo: statementInfoList) {
            if (statementInfo.getChildren() != null && statementInfo.getChildren().size() > 0) {
                for(StatementInfo childStatementInfo: (List<StatementInfo>)statementInfo.getChildren()) {
                    childStatementInfo.setMethodUuid(statementInfo.getMethodUuid());
                    statementInfos.add(childStatementInfo);
                }
            }
            String statementCommitter = statisticsDao.getChangeCommitter("statement", beginDate, statementInfo.getMethodUuid(), statementInfo.getBody(), FORMATTER.format(statementInfo.getCommitDate()));
            if (statementCommitter == null) {
                continue;
            }
            if (committerMap.keySet().contains(statementCommitter)) {
                committerMap.replace(statementCommitter, committerMap.get(statementCommitter) + addOne);
            } else {
                committerMap.put(statementCommitter, addOne);
            }
        }
        return statementInfos;
    }

    @Override
    public Map<String,Map<String,Integer>> getCommitterLineInfo(String repoUuid, String commit, String repoPath, String branch) {
        Map<String,Map<String,Integer>> committerLineInfoMap = new HashMap<>();
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        jGitHelper.checkout(commit);
        RepoInfoBuilder repoInfo = new RepoInfoBuilder(repoUuid, commit, repoPath, jGitHelper, branch, null, null);
        List<CommitterLineInfo> committerLineInfoList = statisticsDao.getCommitterLineInfo(repoInfo.getRepoUuid(),repoInfo.getBranch(),FORMATTER.format(repoInfo.getCommitDate()));
        for (CommitterLineInfo committerLineInfo : committerLineInfoList) {
            Map<String,Integer> map = new HashMap<>();
            if (committerLineInfoMap.keySet().contains(committerLineInfo.getCommitter())) {
                map = committerLineInfoMap.get(committerLineInfo.getCommitter());
                map.replace("ADD",map.get("ADD") + committerLineInfo.getAddCount());
                map.replace("DELETE",map.get("DELETE") + committerLineInfo.getDeleteCount());
                committerLineInfoMap.replace(committerLineInfo.getCommitter(),map);
            } else {
                map.put("ADD",committerLineInfo.getAddCount());
                map.put("DELETE",committerLineInfo.getDeleteCount());
                committerLineInfoMap.put(committerLineInfo.getCommitter(),map);
            }
        }
        return committerLineInfoMap;
    }

}