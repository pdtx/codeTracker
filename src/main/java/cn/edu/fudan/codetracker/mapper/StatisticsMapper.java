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

}
