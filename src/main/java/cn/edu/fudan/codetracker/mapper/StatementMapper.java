package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.StatementInfo;
import java.util.List;

import cn.edu.fudan.codetracker.domain.projectinfo.StatementRelationInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatementMapper {

    void insertStatementInfoList(List<StatementInfo> statementInfos);

    void insertRawStatementInfoList(List<StatementInfo> statementInfos);

    void insertStatementRelationList(List<StatementRelationInfo> statementRelationInfos);

    void updateDeleteInfo(List<StatementInfo> statementInfos);

    void updateChangeInfo(List<StatementInfo> statementInfos);

    TrackerInfo getTrackerInfo(@Param("methodUuid") String methodUuid, @Param("body") String body);

}
