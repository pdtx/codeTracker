/**
 * @description:
 * @author: fancying
 * @create: 2019-11-11 21:28
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private StatisticsDao statisticsDao;

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


    /**
     * getter and setter
     * */
    @Autowired
    public void setStatisticsDao(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }
}