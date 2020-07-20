/**
 * @description:
 * @author: fancying
 * @create: 2019-12-01 21:03
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.*;
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

    public void insertStatementInfoList(List<StatementNode> statementNodes, CommonInfo commonInfo) {
        if (statementNodes.size() == 0) {
            return;
        }
        statementMapper.insertStatementInfoList(statementNodes, commonInfo);
    }

    public void insertRawStatementInfoList(List<StatementNode> statementNodes, CommonInfo commonInfo) {
        if (statementNodes.size() == 0) {
            return;
        }
        statementMapper.insertRawStatementInfoList(statementNodes, commonInfo);
    }

    public void insertStatementRelationList(List<StatementNode> statementNodes, CommonInfo commonInfo) {
        if (statementNodes.size() == 0) {
            return;
        }
        List<StatementRelationInfo> statementRelationInfoList = new ArrayList<>();
        for (StatementNode statementInfo:statementNodes) {
            if (statementInfo.getLevel() == 6){
                continue;
            }
            StatementNode temp = statementInfo;
            String descendantUuid = temp.getUuid();
            int level = temp.getLevel();
            while(temp.getLevel() > 6){
                StatementNode parent = (StatementNode)temp.getParent();
                int parentLevel = parent.getLevel();
                StatementRelationInfo statementRelationInfo = new StatementRelationInfo();
                statementRelationInfo.setUuid(UUID.randomUUID().toString());
                statementRelationInfo.setAncestorUuid(parent.getUuid());
                statementRelationInfo.setDescendantUuid(descendantUuid);
                statementRelationInfo.setDistance(level-parentLevel);
                statementRelationInfo.setValidBegin(commonInfo.getCommitDate());
                statementRelationInfo.setRepoUuid(commonInfo.getRepoUuid());
                statementRelationInfo.setBranch(commonInfo.getBranch());
                statementRelationInfoList.add(statementRelationInfo);
                temp = parent;
            }
        }
        if (statementRelationInfoList.size() > 0) {
            statementMapper.insertStatementRelationList(statementRelationInfoList);
        }
    }

    public void updateDeleteInfo(List<StatementNode> statementNodes, CommonInfo commonInfo) {
        statementMapper.updateDeleteInfo(statementNodes, commonInfo);
    }

    public void updateChangeInfo(List<StatementNode> statementNodes, CommonInfo commonInfo) {
        statementMapper.updateChangeInfo(statementNodes, commonInfo);
    }

    public void setAddInfo(@NotNull Set<StatementNode> statementNodes, CommonInfo commonInfo) {
        if(statementNodes.size()==0){
            return;
        }
        List<StatementNode> statementInfoList = new ArrayList<>(statementNodes);
        insertStatementInfoList(statementInfoList, commonInfo);
        insertRawStatementInfoList(statementInfoList, commonInfo);
        insertStatementRelationList(statementInfoList, commonInfo);
    }

    public void setDeleteInfo(@NotNull Set<StatementNode> statementNodes, CommonInfo commonInfo){
        if(statementNodes.size()==0){
            return;
        }
        List<StatementNode> statementInfoList = new ArrayList<>(statementNodes);
        updateDeleteInfo(statementInfoList, commonInfo);
        insertRawStatementInfoList(statementInfoList, commonInfo);
    }

    public void setChangeInfo(@NotNull Set<StatementNode> statementNodes, CommonInfo commonInfo){
        if(statementNodes.size()==0){
            return;
        }
        List<StatementNode> statementInfoList = new ArrayList<>(statementNodes);
        updateChangeInfo(statementInfoList, commonInfo);
        insertRawStatementInfoList(statementInfoList, commonInfo);
    }

    public TrackerInfo getTrackerInfo(String methodUuid, String body) {
        return statementMapper.getTrackerInfo(methodUuid, body);
    }

    public TrackerInfo getTrackerInfoWithBodyUsingSplice(String methodUuid, String body) {
        return statementMapper.getTrackerInfoWithBodyUsingSplice(methodUuid, body);
    }
}