/**
 * @description: 只记录method以及field中的statement，method 中statement的顺序按照start升序排列
 * @author: fancying
 * @create: 2019-05-26 21:22
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.RelationShip;
import cn.edu.fudan.codetracker.domain.StatementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StatementInfo extends BaseInfo{

    private String uuid;
    private String body;
    private int begin;
    private int end;
    private String methodUuid;
    private TrackerInfo trackerInfo;
    private int level;
    private StatementType type;

    public StatementInfo() {

    }

    public StatementInfo(BaseInfo baseInfo, BaseInfo parent, String body, int begin, int end, String methodUuid) {
        super(baseInfo);
        super.setParent(parent);
        super.setProjectInfoLevel(ProjectInfoLevel.STATEMENT);
        if(parent instanceof StatementInfo){
            level = ((StatementInfo) parent).getLevel() + 1;
        } else {
            level = ProjectInfoLevel.STATEMENT.getLevel();
        }
        this.uuid = UUID.randomUUID().toString();
        this.body = body;
        this.begin = begin;
        this.end = end;
        this.methodUuid = methodUuid;
        trackerInfo =  new TrackerInfo(RelationShip.ADD.name(), 1, uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof StatementInfo){
            StatementInfo statementInfo = (StatementInfo) o;
            return this.uuid.equals(statementInfo.uuid);
        }
        return false;
    }

    /**
     * getter and setter
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public StatementInfo(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getMethodUuid() {
        return methodUuid;
    }

    public void setMethodUuid(String methodUuid) {
        this.methodUuid = methodUuid;
    }

    public StatementType getType() {
        return type;
    }

    public void setType(StatementType type) {
        this.type = type;
    }

    public TrackerInfo getTrackerInfo() {
        return trackerInfo;
    }

    public int getLevel() {
        return level;
    }

    public void setTrackerInfo(String relation, int version, String rootUuid) {
        trackerInfo.setVersion(version);
        trackerInfo.setRootUUID(rootUuid);
        trackerInfo.setChangeRelation(relation);
    }
}