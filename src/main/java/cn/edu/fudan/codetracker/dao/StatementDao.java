/**
 * @description:
 * @author: fancying
 * @create: 2019-12-01 21:03
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.StatementInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.StatementRelationInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.StatementMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class StatementDao {

    private StatementMapper statementMapper;

    @Autowired
    public void setStatementMapper(StatementMapper statementMapper) {
        this.statementMapper = statementMapper;
    }

    public void insertStatementInfoList(List<StatementInfo> statementInfos) {
        statementMapper.insertStatementInfoList(statementInfos);
    }

    public void insertRawStatementInfoList(List<StatementInfo> statementInfos) {
        statementMapper.insertRawStatementInfoList(statementInfos);
    }

    public void insertStatementRelationList(List<StatementInfo> statementInfos) {
        List<StatementRelationInfo> statementRelationInfoList = new ArrayList<>();
        for (StatementInfo statementInfo:statementInfos) {
            if (statementInfo.getLevel() == 6){
                continue;
            }
            StatementInfo temp = statementInfo;
            String descendantUuid = temp.getUuid();
            int level = temp.getLevel();
            while(temp.getLevel() > 6){
                StatementInfo parent = (StatementInfo)temp.getParent();
                int parentLevel = parent.getLevel();
                StatementRelationInfo statementRelationInfo = new StatementRelationInfo();
                statementRelationInfo.setUuid(UUID.randomUUID().toString());
                statementRelationInfo.setAncestorUuid(parent.getUuid());
                statementRelationInfo.setDescendantUuid(descendantUuid);
                statementRelationInfo.setDistance(level-parentLevel);
                statementRelationInfo.setValidBegin(temp.getCommitDate());
                statementRelationInfo.setRepoUuid(temp.getRepoUuid());
                statementRelationInfo.setBranch(temp.getBranch());
                statementRelationInfoList.add(statementRelationInfo);
                temp = parent;
            }
        }
        if (statementRelationInfoList.size() > 0) {
            statementMapper.insertStatementRelationList(statementRelationInfoList);
        }
    }

    public void updateDeleteInfo(List<StatementInfo> statementInfos) {
        statementMapper.updateDeleteInfo(statementInfos);
    }

    public void updateChangeInfo(List<StatementInfo> statementInfos) {
        statementMapper.updateChangeInfo(statementInfos);
    }

    public TrackerInfo getTrackerInfo(String methodUuid, String body) {
        return statementMapper.getTrackerInfo(methodUuid, body);
    }

    public void setAddInfo(@NotNull Set<StatementInfo> statementInfos) {
        if(statementInfos.size()==0){
            return;
        }
        List<StatementInfo> statementInfoList = new ArrayList<>(statementInfos);
        insertStatementInfoList(statementInfoList);
        insertRawStatementInfoList(statementInfoList);
        insertStatementRelationList(statementInfoList);
    }

    public void setDeleteInfo(@NotNull Set<StatementInfo> statementInfos){
        if(statementInfos.size()==0){
            return;
        }
        List<StatementInfo> statementInfoList = new ArrayList<>(statementInfos);
        updateDeleteInfo(statementInfoList);
        insertRawStatementInfoList(statementInfoList);
    }

    public void setChangeInfo(@NotNull Set<StatementInfo> statementInfos){
        if(statementInfos.size()==0){
            return;
        }
        List<StatementInfo> statementInfoList = new ArrayList<>(statementInfos);
        updateChangeInfo(statementInfoList);
        insertRawStatementInfoList(statementInfoList);
    }


    public TrackerInfo getTrackerInfoWithBodyUsingSplice(String methodUuid, String body) {
        return statementMapper.getTrackerInfoWithBodyUsingSplice(methodUuid, body);
    }
}