package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.resultmap.MethodHistory;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.StatementInfoByMethod;
import cn.edu.fudan.codetracker.domain.resultmap.SurviveStatementInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryMapper {

    /**
     * 获取语句历史切片
     */
    List<SurviveStatementInfo> getStatementHistory(@Param("methodUuid") String methodUuid, @Param("body") String body, @Param("commitId") String commitId);

    /**
     * 尝试一次查询出所有有效语句
     */
    List<StatementInfoByMethod> getAllValidStatement(@Param("methodUuid") String methodUuid, @Param("commitDate") String commitDate);

    /**
     * 获取method历史，默认近两个月
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
    String getMethodUuid(@Param(value="methodHistory") MethodHistory methodHistory);

    /**
     * 获取method的package、class等信息
     */
    MostModifiedInfo getMethodMetaInfo(@Param("methodUuid") String methodUuid);


}
