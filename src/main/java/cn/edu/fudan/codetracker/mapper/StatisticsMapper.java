package cn.edu.fudan.codetracker.mapper;

/**
 * @author: fancying
 * @create: 2019-06-06 16:41
 */
import cn.edu.fudan.codetracker.domain.resultmap.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsMapper {
    /**
     * get valid line info by class
     */
    List<ValidLineInfo> getValidLineInfoByClass(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * get valid line info by method
     */
    List<ValidLineInfo> getValidLineInfoByMethod(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * get valid line info by field
     */
    List<ValidLineInfo> getValidLineInfoByField(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * get valid line info by statement
     */
    List<ValidLineInfo> getValidLineInfoByStatement(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);


    /**
     * 获取时间段内存活代码情况
     */
    List<SurviveStatementInfo> getSurviveStatement(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch);
    List<SurviveStatementInfo> getSurviveMethod(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch);
    List<SurviveStatementInfo> getSurviveField(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch);


    /**
     * 删除项目相关追溯数据
     */
    void deleteFromMetaPackage(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromMetaFile(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromMetaClass(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromMetaMethod(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromMetaField(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromMetaStatement(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromRawPackage(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromRawFile(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromRawClass(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromRawMethod(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromRawField(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromRawStatement(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromRelationStatement(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    void deleteFromTrackerRepo(@Param("repoUuid") String repoUuid, @Param("branch") String branch);


    //暂时未用到

    /**
     * distribution of method modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getMethodStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * distribution of class modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getClassStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * distribution of file modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getFileStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * distribution of package modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getPackageStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);



    /**
     * most modified method
     */
    List<MostModifiedInfo> getMostModifiedMethod(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * most modified file
     */
    List<MostModifiedInfo> getMostModifiedFile(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * most modified class
     */
    List<MostModifiedInfo> getMostModifiedClass(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * most modified package
     */
    List<MostModifiedInfo> getMostModifiedPackage(@Param("repoUuid") String repoUuid, @Param("branch") String branch);


    /**
     * file modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedFile(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * class modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedClass(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * method modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedMethod(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * package modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedPackage(@Param("repoUuid") String repoUuid, @Param("branch") String branch);



    /**
     * most modified package in given time
     */
    List<MostDevelopersInfo> getMostModifiedPackageByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * most modified file in given time
     */
    List<MostDevelopersInfo> getMostModifiedFileByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * most modified class in given time
     */
    List<MostDevelopersInfo> getMostModifiedClassByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * most modified method in given time
     */
    List<MostDevelopersInfo> getMostModifiedMethodByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);


    /**
     * get most modified methods info in given package
     */
    List<MostModifiedMethod> getMostModifiedMethodByPackage(@Param("repoUuid") String repoUuid, @Param("packageUuid") String packageUuid, @Param("branch") String branch);


    /**
     * package that developer most focus on in given time
     */
    List<DeveloperMostFocus> packageDeveloperFocusMost(@Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * file that developer most focus on in given time
     */
    List<DeveloperMostFocus> fileDeveloperFocusMost(@Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * class that developer most focus on in given time
     */
    List<DeveloperMostFocus> classDeveloperFocusMost(@Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * method that developer most focus on in given time
     */
    List<DeveloperMostFocus> methodDeveloperFocusMost(@Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);


    /**
     * get package commit message
     */
    List<String> getCommitMessageByPackageId(@Param("uuid") String uuid, @Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * get file commit message
     */
    List<String> getCommitMessageByFileId(@Param("uuid") String uuid, @Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * get class commit message
     */
    List<String> getCommitMessageByClassId(@Param("uuid") String uuid, @Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * get method commit message
     */
    List<String> getCommitMessageByMethodId(@Param("uuid") String uuid, @Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);


    /**
     * get method content
     */
    List<String> getContentByMethodId(@Param("uuid") String uuid, @Param("committer") String committer, @Param("beginDate") String beginDate, @Param("endDate") String endDate);


    /**
     * get package commit info
     */
    List<CommitTimeLine> packageCommitTimeLine(@Param("uuid") String uuid);
    /**
     * get file commit info
     */
    List<CommitTimeLine> fileCommitTimeLine(@Param("uuid") String uuid);
    /**
     * get class commit info
     */
    List<CommitTimeLine> classCommitTimeLine(@Param("uuid") String uuid);
    /**
     * get method commit info
     */
    List<CommitTimeLine> methodCommitTimeLine(@Param("uuid") String uuid);


    /**
     * get commit list by committer
     */
    List<CommitInfoByCommitter> getCommitInfoByCommitter(@Param("committer") String committer);


    /**
     * get file info by commitId
     */
    List<BasicInfoByCommitId> getFileInfoByCommitId(@Param("commitId") String commitId);
    /**
     * get method info by commitId
     */
    List<BasicInfoByCommitId> getMethodInfoByCommitId(@Param("commitId") String commitId);


    /**
     * get delete statement uuid by committer in given time
     */
    List<DeleteStatementInfo> getDeleteStatementUuidList(@Param("committer") String committer, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);

    /**
     * get delete statement last info
     */
    DeleteStatementInfo getDeleteStatementLastInfo(@Param("statementUuid") String statementUuid);

    /**
     * get delete statement first info
     */
    DeleteStatementInfo getDeleteStatementFirstInfo(@Param("statementUuid") String statementUuid);


    /**
     * get statement info by method and committer in given time
     */
    List<StatementInfoByMethod> getStatementInfoByMethod(@Param("committer") String committer, @Param("methodUuid") String methodUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate);

    /**
     * get change committer by class
     */
    String getChangeCommitterByClass(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("commitDate") String commitDate);
    /**
     * get change committer by method
     */
    String getChangeCommitterByMethod(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("signature") String signature, @Param("commitDate") String commitDate);
    /**
     * get change committer by field
     */
    String getChangeCommitterByField(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("simpleName") String simpleName, @Param("commitDate") String commitDate);
    /**
     * get change committer by statement
     */
    String getChangeCommitterByStatement(@Param("methodUuid") String methodUuid, @Param("body") String body, @Param("commitDate") String commitDate);
    /**
     * get meta method uuid by method
     */
    String getMetaMethodUuidByMethod(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("signature") String signature, @Param("commitDate") String commitDate);


    /**
     * get change committer by class beginDate
     */
    String getChangeCommitterByClassDate(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("commitDate") String commitDate, @Param("beginDate") String beginDate);
    /**
     * get change committer by method beginDate
     */
    String getChangeCommitterByMethodDate(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("signature") String signature, @Param("commitDate") String commitDate, @Param("beginDate") String beginDate);
    /**
     * get change committer by field beginDate
     */
    String getChangeCommitterByFieldDate(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("simpleName") String simpleName, @Param("commitDate") String commitDate, @Param("beginDate") String beginDate);
    /**
     * get change committer by statement beginDate
     */
    String getChangeCommitterByStatementDate(@Param("methodUuid") String methodUuid, @Param("body") String body, @Param("commitDate") String commitDate, @Param("beginDate") String beginDate);
    /**
     * get meta method uuid by method beginDate
     */
    String getMetaMethodUuidByMethodDate(@Param("filePath") String filePath, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("className") String className, @Param("signature") String signature, @Param("commitDate") String commitDate, @Param("beginDate") String beginDate);

    /**
     * get committer line info by commit
     */
    List<CommitterLineInfo> getCommitterLineInfo(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("commitDate") String commitDate);

}
