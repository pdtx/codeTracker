package cn.edu.fudan.codetracker.service;

import cn.edu.fudan.codetracker.domain.resultmap.*;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    List<VersionStatistics> getStatistics(String repoUuid, String branch, String type);

    List<MostModifiedInfo> getMostModifiedInfo(String repoUuid, String branch, String type);

    List<MostDevelopersInfo> getMostDevelopersInvolved(String repoUuid, String branch, String type);

    /**
    resultMap数据结构待议
     */
    List<MostDevelopersInfo> getMostModifiedByTime(String repoUuid, String branch, String type, String beginDate, String endDate);

    List<MostModifiedMethod> getMostModifiedMethodByPackage(String repoUuid, String packageUuid, String branch);

    List<DeveloperMostFocus> getDeveloperFocusMost(String type, String committer,String beginDate, String endDate);

    List<CommitTimeLine> getCommitTimeLine(String type, String uuid);

    List<CommitterHistory> getCommitHistoryByCommitter(String committer);

    List<DeleteStatementInfo> getDeleteStatementFormerInfoByCommitter(String committer, String repoUuid, String branch, String beginDate, String endDate);

    List<StatementInfoByMethod> getStatementInfoByMethod(String committer, String methodUuid, String beginDate, String endDate);

    Map<String,Integer> getChangeCommitterInfo(String repoUuid, String commit, String repoPath, String branch);

    Map<String,Map<String,Integer>> getCommitterLineInfo(String repoUuid, String commit, String repoPath, String branch);

    /**
     临时演示接口
     */
    List<TempMostInfo> getFocus(String committer);
    /**
     method历史接口
     */
    List<MethodHistory> getMethodHistory(String methodUuid);
}
