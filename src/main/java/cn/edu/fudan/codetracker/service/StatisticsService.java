package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.MostDevelopersInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedMethod;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;

import java.util.List;

public interface StatisticsService {

    List<VersionStatistics> getStatistics(String repoUuid, String branch, String type);

    List<MostModifiedInfo> getMostModifiedInfo(String repoUuid, String branch, String type);

    List<MostDevelopersInfo> getMostDevelopersInvolved(String repoUuid, String branch, String type);

    /**
    resultMap数据结构待议
     */
    List<MostDevelopersInfo> getMostModifiedByTime(String repoUuid, String branch, String type, String beginDate, String endDate);

    List<MostModifiedMethod> getMostModifiedMethodByPackage(String repoUuid, String packageUuid, String branch);

}
