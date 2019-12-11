package cn.edu.fudan.codetracker.domain.resultmap;

import java.util.Date;

public class DeleteStatementInfo {
    private String statementUuid;
    private String formerCommitter;
    private Date formerCommitDate;
    private String body;

    DeleteStatementInfo() {

    }

    public String getStatementUuid() { return statementUuid; }

    public String getFormerCommitter() { return formerCommitter; }

    public Date getFormerCommitDate() { return formerCommitDate; }

    public String getBody() { return body; }

    public void setStatementUuid(String statementUuid) { this.statementUuid = statementUuid; }

    public void setFormerCommitter(String formerCommitter) { this.formerCommitter = formerCommitter; }

    public void setFormerCommitDate(Date formerCommitDate) { this.formerCommitDate = formerCommitDate; }

    public void setBody(String body) { this.body = body; }

}
