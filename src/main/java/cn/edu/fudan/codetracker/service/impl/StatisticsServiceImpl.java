/**
 * @description:
 * @author: fancying
 * @create: 2019-11-11 21:28
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.resultmap.MostDevelopersInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedMethod;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
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


    /**
     * getter and setter
     * */
    @Autowired
    public void setStatisticsDao(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }
}