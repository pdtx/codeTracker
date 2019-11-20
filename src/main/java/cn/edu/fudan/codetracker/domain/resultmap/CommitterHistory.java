package cn.edu.fudan.codetracker.domain.resultmap;

import java.util.List;

public class CommitterHistory {
    private String commitId;
    private String commitDate;
    private String commitMessage;
    private List<BasicInfoByCommitId> fileList;
    private List<BasicInfoByCommitId> methodList;

    public CommitterHistory() {

    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitMessage() { return commitMessage; }

    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public List<BasicInfoByCommitId> getFileList() {
        return fileList;
    }

    public void setFileList(List<BasicInfoByCommitId> fileList) {
        this.fileList = fileList;
    }

    public List<BasicInfoByCommitId> getMethodList() {
        return methodList;
    }

    public void setMethodList(List<BasicInfoByCommitId> methodList) {
        this.methodList = methodList;
    }
}
