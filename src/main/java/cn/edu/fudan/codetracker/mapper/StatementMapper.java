package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.*;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementMapper {

    void insertStatementInfoList(@Param("statementNodes") List<StatementNode> statementNodes, @Param("commonInfo") CommonInfo commonInfo);

    void insertRawStatementInfoList(@Param("statementNodes") List<StatementNode> statementNodes, @Param("commonInfo") CommonInfo commonInfo);

    void insertStatementRelationList(List<StatementRelationInfo> statementRelationInfos);

    void updateChangeInfo(@Param("statementNodes") List<StatementNode> statementNodes, @Param("commonInfo") CommonInfo commonInfo);

    void updateDeleteInfo(@Param("statementNodes") List<StatementNode> statementNodes, @Param("commonInfo") CommonInfo commonInfo);



    TrackerInfo getTrackerInfo(@Param("methodUuid") String methodUuid, @Param("body") String body);

    TrackerInfo getTrackerInfoWithBodyUsingSplice(@Param("methodUuid")String methodUuid, @Param("body") String body);
}
