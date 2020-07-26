package cn.edu.fudan.codetracker.domain.resultmap;


import java.util.Date;

public class SurviveStatementInfo {
    private String statementUuid;
    private String committer;
    private String commitDate;
    private String changeRelation;
    private String commitMessage;
    private String commit;
    private String body;
    private int begin;
    private int end;

    public SurviveStatementInfo() {

    }

    public String getCommitMessage() { return commitMessage; }

    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public String getStatementUuid() {
        return statementUuid;
    }

    public void setStatementUuid(String statementUuid) {
        this.statementUuid = statementUuid;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getChangeRelation() {
        return changeRelation;
    }

    public void setChangeRelation(String changeRelation) {
        this.changeRelation = changeRelation;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
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

}
