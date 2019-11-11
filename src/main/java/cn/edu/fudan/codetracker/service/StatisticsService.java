package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;

import java.util.List;

public interface StatisticsService {

    List<VersionStatistics> getMethodStatistics(String repoUuid, String branch);

}
