package cn.edu.fudan.codetracker.domain.resultmap;

public class ValidLineInfo {
    private String uuid;
    private String committer;
    private String metaUuid;
    private String changeRelation;
    private String commitDate;

    public ValidLineInfo() {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getMetaUuid() {
        return metaUuid;
    }

    public void setMetaUuid(String metaUuid) {
        this.metaUuid = metaUuid;
    }

    public String getChangeRelation() {
        return changeRelation;
    }

    public void setChangeRelation(String changeRelation) {
        this.changeRelation = changeRelation;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }
}
