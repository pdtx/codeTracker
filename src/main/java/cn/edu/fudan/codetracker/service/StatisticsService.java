package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.*;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    /**
     * 优化有效行数查询
     */
    Map<String,Integer> getValidLineCount(String repoUuid, String branch, String beginDate, String endDate);

    /**
     * 统计存活周期
     */
    Map<String,Map<String,Double>> getSurviveStatementStatistics(String beginDate, String endDate, String repoUuid, String branch);

    /**
     * 删除操作
     */
    void delete(String repoUuid, String branch);


}
