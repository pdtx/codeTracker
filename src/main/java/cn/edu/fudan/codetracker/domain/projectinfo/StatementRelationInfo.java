package cn.edu.fudan.codetracker.domain.projectinfo;

import java.util.Date;

public class StatementRelationInfo {

    private String uuid;
    private String ancestorUuid;
    private String descendantUuid;
    private int distance;
    private Date validBegin;
    private Date validEnd;
    private String repoUuid;
    private String branch;

    public StatementRelationInfo () {

    }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getAncestorUuid() { return ancestorUuid; }

    public void setAncestorUuid(String ancestorUuid) { this.ancestorUuid = ancestorUuid; }

    public String getDescendantUuid() { return descendantUuid; }

    public void setDescendantUuid(String descendantUuid) { this.descendantUuid = descendantUuid; }

    public int getDistance() { return distance; }

    public void setDistance(int distance) { this.distance = distance; }

    public Date getValidBegin() { return validBegin; }

    public void setValidBegin(Date validBegin) { this.validBegin = validBegin; }

    public Date getValidEnd() { return validEnd; }

    public void setValidEnd(Date validEnd) { this.validEnd = validEnd; }

    public String getRepoUuid() {
        return repoUuid;
    }

    public void setRepoUuid(String repoUuid) {
        this.repoUuid = repoUuid;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
