package cn.edu.fudan.codetracker.domain;

import java.util.Date;

public class LineInfo {
    private String commitId;
    private int lineCount;
    private int importCount;
    private int addCount;
    private int deleteCount;
    private String committer;
    private Date commitDate;

    public LineInfo() {

    }

    public String getCommitId() { return commitId; }

    public int getLineCount() { return lineCount; }

    public int getImportCount() { return importCount; }

    public int getAddCount() { return addCount; }

    public int getDeleteCount() { return deleteCount; }

    public String getCommitter() { return committer; }

    public Date getCommitDate() { return commitDate; }

    public void setCommitId(String commitId) { this.commitId = commitId; }

    public void setLineCount(int lineCount) { this.lineCount = lineCount; }

    public void setImportCount(int importCount) { this.importCount = importCount; }

    public void setAddCount(int addCount) { this.addCount = addCount; }

    public void setDeleteCount(int deleteCount) { this.deleteCount = deleteCount; }

    public void setCommitter(String committer) { this.committer = committer; }

    public void setCommitDate(Date commitDate) { this.commitDate = commitDate; }
}
