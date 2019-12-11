package cn.edu.fudan.codetracker.domain.resultmap;

import java.util.Date;

public class StatementInfoByMethod {
    private String statementUuid;
    private String committer;
    private Date commitDate;
    private String commitMessage;
    private String changeRelation;
    private String body;
    private int begin;
    private int end;

    StatementInfoByMethod() {

    }

    public String getStatementUuid() { return statementUuid; }

    public String getCommitter() { return committer; }

    public Date getCommitDate() { return commitDate; }

    public String getCommitMessage() { return commitMessage; }

    public String getChangeRelation() { return changeRelation; }

    public String getBody() { return body; }

    public int getBegin() { return begin; }

    public int getEnd() { return end; }

    public void setStatementUuid(String statementUuid) { this.statementUuid = statementUuid; }

    public void setCommitter(String committer) { this.committer = committer; }

    public void setCommitDate(Date commitDate) { this.commitDate = commitDate; }

    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public void setChangeRelation(String changeRelation) { this.changeRelation = changeRelation; }

    public void setBody(String body) { this.body = body; }

    public void setBegin(int begin) { this.begin = begin; }

    public void setEnd(int end) { this.end = end; }
}
