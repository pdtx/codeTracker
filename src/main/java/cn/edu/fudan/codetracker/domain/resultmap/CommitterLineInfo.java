package cn.edu.fudan.codetracker.domain.resultmap;

public class CommitterLineInfo {
    private String commitId;
    private String committer;
    private int addCount;
    private int deleteCount;

    CommitterLineInfo() {

    }

    public String getUuid() { return commitId; }

    public String getCommitter() { return committer; }

    public int getAddCount() { return addCount; }

    public int getDeleteCount() { return deleteCount; }

    public void setUuid(String commitId) { this.commitId = commitId; }

    public void setCommitter(String committer) { this.committer = committer; }

    public void setAddCount(int addCount) { this.addCount = addCount; }

    public void setDeleteCount(int deleteCount) { this.deleteCount = deleteCount; }
}
