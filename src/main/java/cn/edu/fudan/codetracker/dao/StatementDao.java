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
import com.sun.jmx.remote.internal.ArrayQueue;
import org.apache.kafka.common.protocol.types.Field;
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
                statementRelationInfoList.add(statementRelationInfo);
                temp = parent;
            }
        }
        statementMapper.insertStatementRelationList(statementRelationInfoList);
    }

    public void updateDeleteInfo(List<StatementInfo> statementInfos) {
        statementMapper.updateDeleteInfo(statementInfos);
    }

    public void updateChangeInfo(List<StatementInfo> statementInfos) {
        statementMapper.updateChangeInfo(statementInfos);
    }

    public TrackerInfo getTrackerInfo(String methodUuid, String begin, String end, String body) {
        return statementMapper.getTrackerInfo(methodUuid, begin, end, body);
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


}