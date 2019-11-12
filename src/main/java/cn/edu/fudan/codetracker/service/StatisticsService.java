package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;

import java.util.List;

public interface StatisticsService {

    List<VersionStatistics> getStatistics(String repoUuid, String branch, String type);

    List<MostModifiedInfo> getMostModifiedInfo(String repoUuid, String branch, String type);

    public List<VersionStatistics> getMostDevelopersInvolved(String repoUuid, String branch, String type);

}
