package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.MethodHistory;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.SurviveStatementInfo;
import cn.edu.fudan.codetracker.domain.resultmap.TempMostInfo;

import java.util.List;
import java.util.Map;

public interface HistoryService {
    /**
     * 获取语句历史
     */
    List<Map<String, Map<String, List<SurviveStatementInfo>>>> getStatementHistory(String methodUuid, List<String> statementBodyList);

    /**
     * 获取所有可选语句
     */
    List<Map<String,String>> getAllValidStatement(String methodUuid, String commitDate, String body);

    /**
     method历史接口
     */
    List<MethodHistory> getMethodHistory(String methodUuid);

    /**
     临时演示接口
     */
    List<TempMostInfo> getFocus(String committer, String beginDate, String endDate, String repoUuid, String branch);

    /**
     * 根据issueList中信息查找对应的methodUuid，有filePath,commitId,issue行号,repoUuid等
     */
    String getMethodUuid(MethodHistory methodHistory);

    /**
     * 获取method的package、class等信息
     */
    MostModifiedInfo getMethodMetaInfo(String methodUuid);

}
