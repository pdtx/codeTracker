package cn.edu.fudan.codetracker.domain.resultmap;

import java.util.Date;

public class DeleteStatementInfo {
    private String statementUuid;
    private String lastCommitter;
    private Date lastCommitDate;
    private String body;
    private String firstCommitter;
    private Date firstCommitDate;
    private String deleteCommitter;
    private Date deleteDate;


    DeleteStatementInfo() {

    }

    public String getStatementUuid() { return statementUuid; }

    public String getLastCommitter() { return lastCommitter; }

    public Date getLastCommitDate() { return lastCommitDate; }

    public String getBody() { return body; }

    public String getFirstCommitter() { return firstCommitter; }

    public Date getFirstCommitDate() { return firstCommitDate; }

    public String getDeleteCommitter() { return deleteCommitter; }

    public Date getDeleteDate() { return deleteDate; }

    public void setLastCommitter(String lastCommitter) { this.lastCommitter = lastCommitter; }

    public void setLastCommitDate(Date lastCommitDate) { this.lastCommitDate = lastCommitDate; }

    public void setStatementUuid(String statementUuid) { this.statementUuid = statementUuid; }

    public void setBody(String body) { this.body = body; }

    public void setFirstCommitter(String firstCommitter) { this.firstCommitter = firstCommitter; }

    public void setFirstCommitDate(Date firstCommitDate) { this.firstCommitDate = firstCommitDate; }

    public void setDeleteCommitter(String deleteCommitter) { this.deleteCommitter = deleteCommitter; }

    public void setDeleteDate(Date deleteDate) { this.deleteDate = deleteDate; }
}
