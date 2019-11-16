package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.*;

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

    List<MostDevelopersInfo> getDeveloperFocusMost(String repoUuid, String type, String branch, String committer,String beginDate, String endDate);

    List<CommitTimeLine> getCommitTimeLine(String type, String uuid);

}
