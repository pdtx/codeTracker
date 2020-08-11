package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.*;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    /**
     * 优化有效行数查询
     */
    Map<String,Map<String, Integer>> getValidLineCount(String repoUuid, String branch, String beginDate, String endDate, String developer);

    /**
     * 统计存活周期
     */
    Map<String,Map<String,Double>> getSurviveStatementStatistics(String beginDate, String endDate, String repoUuid, String branch);

    /**
     * 删除操作
     */
    void delete(String repoUuid, String branch);

    /**
     * 获取新增、删除语句数
     * @param beginDate
     * @param endDate
     * @param repoUuid
     * @param branch
     * @param developer
     * @return
     */
    Map<String, Map<String,Map<String,Integer>>> getAddDeleteStatementsNumber(String beginDate, String endDate, String repoUuid, String branch, String developer);

    /**
     * 获取变更语句的年龄
     * @param beginDate
     * @param endDate
     * @param repoUuid
     * @param branch
     * @return
     */
    Map<String, Map<String, Double>> getChangeStatementsLifecycle(String beginDate, String endDate, String repoUuid, String branch);

    /**
     * 获取删除代码年代信息 max average
     * @param beginDate
     * @param endDate
     * @param repoUuid
     * @return
     */
    JSONObject getDeleteInfo(String beginDate, String endDate, String repoUuid);

    /**
     * 获取存活语句数排名前五的开发者信息
     * @param repoUuid
     * @param beginDate
     * @param endDate
     * @return
     */
    List<JSONObject> getTop5LiveStatements(String repoUuid, String beginDate, String endDate);


    /**
     * 获取修改代码信息 max average
     * @param beginDate
     * @param endDate
     * @param repoUuid
     * @return
     */
    JSONObject getChangeInfo(String beginDate,String endDate,String repoUuid);


    /**
     * 获取工作焦点文件数
     * @param repoUuid
     * @param beginDate
     * @param endDate
     * @return
     */
    JSONObject getFocusFileNum(String repoUuid, String beginDate, String endDate);

    /**
     * 修改文件个数 前端接口
     * @param repoUuid
     * @param beginDate
     * @param endDate
     * @return
     */
    JSONObject getFileNum(String repoUuid, String beginDate, String endDate);



}
