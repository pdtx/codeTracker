package cn.edu.fudan.codetracker.domain.resultmap;

public class CommitInfoByCommitter {
    private String commitId;
    private String commitDate;

    CommitInfoByCommitter() {

    }

    public String getCommitId() { return commitId; }

    public void setCommitId(String commitId) { this.commitId = commitId; }

    public String getCommitDate() { return commitDate; }

    public void setCommitDate(String commitDate) { this.commitDate = commitDate; }
}
