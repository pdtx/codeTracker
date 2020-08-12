package cn.edu.fudan.codetracker.mapper;

/**
 * @author: fancying
 * @create: 2019-06-06 16:41
 */
import cn.edu.fudan.codetracker.domain.resultmap.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StatisticsMapper {
    /**
     * get valid line info by class
     */
    List<ValidLineInfo> getValidLineInfoByClass(@Param("repoUuid") String repoUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer);
    /**
     * get valid line info by method
     */
    List<ValidLineInfo> getValidLineInfoByMethod(@Param("repoUuid") String repoUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer);
    /**
     * get valid line info by field
     */
    List<ValidLineInfo> getValidLineInfoByField(@Param("repoUuid") String repoUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer);
    /**
     * get valid line info by statement
     */
    List<ValidLineInfo> getValidLineInfoByStatement(@Param("repoUuid") String repoUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer);


    /**
     * 获取时间段内存活代码情况
     */
    List<SurviveStatementInfo> getSurviveStatement(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("developer") String developer);
    List<SurviveStatementInfo> getSurviveMethod(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("developer") String developer);
    List<SurviveStatementInfo> getSurviveField(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("developer") String developer);
    List<SurviveStatementInfo> getSurviveClass(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("developer") String developer);

    /**
     * （根据committer）获取所有repo
     * @param beginDate
     * @param endDate
     * @param developer
     * @return
     */
    List<String> getDistinctRepoUuid(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer);

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
    void deleteFromMethodCall(@Param("repoUuid") String repoUuid);

    /**
     * 获取删除代码信息 用于计算所删除代码年代
     * @param beginDate
     * @param endDate
     * @param repoUuid
     * @param developer
     * @return
     */
    List<SurviveStatementInfo> getDeleteInfo(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("developer") String developer);


    /**
     * 获取修改代码信息
     * @param beginDate
     * @param endDate
     * @param repoUuid
     * @return
     */
    List<SurviveStatementInfo> getChangeInfo(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid);



    /**
     * 获取工作焦点文件信息 计算文件数
     * @param repoUuid
     * @param beginDate
     * @param endDate
     * @return
     */
    List<TempMostInfo> getFocusFileNum(@Param("repoUuid") String repoUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate);

    /**
     * 获取语句的首个提交者
     * @return key: meta_uuid, value: committer
     */
    @MapKey("meta_statement_uuid")
    Map<String, Map<String, String>> getStatementFirstCommitter(@Param("metaList") List<String> metaList);

    @MapKey("meta_field_uuid")
    Map<String, Map<String, String>> getFieldFirstCommitter(@Param("metaList") List<String> metaList);

    @MapKey("meta_method_uuid")
    Map<String, Map<String, String>> getMethodFirstCommitter(@Param("metaList") List<String> metaList);

    @MapKey("meta_class_uuid")
    Map<String, Map<String, String>> getClassFirstCommitter(@Param("metaList") List<String> metaList);

}
