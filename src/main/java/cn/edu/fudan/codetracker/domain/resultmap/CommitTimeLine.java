package cn.edu.fudan.codetracker.domain.resultmap;
import java.io.Serializable;
import java.util.Date;

public class CommitTimeLine {
    private String commitId;
    private String committer;
    private String commitDate;
    private String changeRelation;
    private String commitMessage;

    CommitTimeLine() {

    }

    public String getCommitId() { return commitId; }

    public void setCommitId(String commitId) { this.commitId = commitId; }

    public String getCommitter() { return committer; }

    public void setCommitter(String committer) { this.committer = committer; }

    public String getCommitDate() { return commitDate; }

    public void setCommitDate(String commitDate) { this.commitDate = commitDate; }

    public String getChangeRelation() { return changeRelation; }

    public void setChangeRelation(String changeRelation) { this.changeRelation = changeRelation; }

    public String getCommitMessage() { return commitMessage; }

    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }
}
