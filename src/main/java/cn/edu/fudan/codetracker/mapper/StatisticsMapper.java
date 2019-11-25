package cn.edu.fudan.codetracker.mapper;

/**
 * @author: fancying
 * @create: 2019-06-06 16:41
 */
import cn.edu.fudan.codetracker.domain.resultmap.*;
import org.apache.ibatis.annotations.Param;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsMapper {
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



}
