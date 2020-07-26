package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.MethodCall;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HistoryMapper {

    /**
     * 获取语句历史切片
     */
    List<SurviveStatementInfo> getStatementHistory(@Param("methodUuid") String methodUuid, @Param("body") String body, @Param("commitId") String commitId);

    /**
     * 尝试一次查询出所有有效语句
     */
    List<SurviveStatementInfo> getAllValidStatement(@Param("methodUuid") String methodUuid, @Param("commitDate") String commitDate);

    /**
     * 获取method历史
     */
    List<MethodHistory> getMethodHistory(@Param("methodUuid") String methodUuid);

    /**
     * 演示临时接口 工作焦点
     */
    List<MostModifiedInfo> getPackageInfoMost(@Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * 演示临时接口 工作焦点
     */
    List<MostModifiedInfo> getFileInfoMost(@Param("committer") String committer, @Param("packageName") String packageName, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * 演示临时接口 工作焦点
     */
    List<MostModifiedInfo> getClassInfoMost(@Param("committer") String committer, @Param("filePath") String filePath, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * 演示临时接口 工作焦点
     */
    List<MostModifiedInfo> getMethodInfoMost(@Param("committer") String committer, @Param("filePath") String filePath, @Param("className") String className, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch);


    /**
     * 根据issueList中信息查找对应的methodUuid，有filePath,commitId,issue行号,repoUuid等
     */
    MethodHistory getMethodInfo(@Param("repoUuid") String repoUuid, @Param("filePath") String filePath, @Param("commitTime") String commitTime, @Param("methodName") String methodName);

    /**
     * 获取method的package、class等信息
     */
    MostModifiedInfo getMethodMetaInfo(@Param("methodUuid") String methodUuid);


    /**
     * 获取bug所在语句
     */
    List<ValidLineInfo> getBugStatement(@Param("methodUuid") String methodUuid, @Param("commitTime") String commitTime, @Param("body") String body);


    /**
     * 代码历史重演 某段时间修改过的所有文件
     */
    List<MostModifiedInfo> getFileInfo(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid);

    /**
     * 代码历史重演 某段时间修改过的某个文件的所有方法
     */
    List<MostModifiedInfo> getMethodInfoByFile(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid);

    /**
     * 获取语句历史切片 statementUuid
     */
    List<SurviveStatementInfo> getStatementHistoryById(@Param("statementUuid") String statementUuid);

    /**
     * 代码历史重演 某次commit修改过的所有文件
     */
    List<MostModifiedInfo> getFileInfoByCommit(@Param("repoUuid") String repoUuid, @Param("commitId") String commitId);

    /**
     * 获取方法上一个版本信息
     * @param methodUuidList
     * @param commitId
     * @return
     */
    List<MostModifiedInfo> getMethodLastInfo(@Param("methodUuidList") Set<String> methodUuidList, @Param("commitId") String commitId);

    /**
     * 代码历史重演 某次commit修改过的某个文件的所有方法
     */
    List<MostModifiedInfo> getMethodInfoByCommit(@Param("repoUuid") String repoUuid, @Param("commitId") String commitId);

    /**
     * 代码历史重演 某次commit修改过的某个文件的某个方法所有代码片段变更
     */
    List<MostModifiedInfo> getStatementInfoByCommit(@Param("repoUuid") String repoUuid, @Param("commitId") String commitId);

    List<MethodCall> getMethodCallsByCommit(@Param("repoUuid") String repoUuid, @Param("commitId") String commitId);

}
