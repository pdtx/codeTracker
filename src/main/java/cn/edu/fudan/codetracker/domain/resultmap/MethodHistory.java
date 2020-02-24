package cn.edu.fudan.codetracker.domain.resultmap;

import com.alibaba.fastjson.JSONObject;
import java.util.List;
import java.util.Map;

public class MethodHistory {
    private String commit;
    private String committer;
    private String commitMessage;
    private String commitDate;
    private String content;
    private com.alibaba.fastjson.JSONObject diff;
    private String changeRelation;
    private int methodBegin;
    private int methodEnd;
    private String parentCommit;
    private List<Map<String,String>> validMapList;


    MethodHistory() {

    }

    public String getCommitter() { return committer; }

    public String getCommitDate() { return commitDate; }

    public String getCommitMessage() { return commitMessage; }

    public String getContent() { return content; }

    public String getChangeRelation() { return changeRelation; }

    public int getMethodBegin() { return methodBegin; }

    public int getMethodEnd() { return methodEnd; }

    public void setCommitter(String committer) { this.committer = committer; }

    public void setCommitDate(String commitDate) { this.commitDate = commitDate; }

    public void setChangeRelation(String changeRelation) { this.changeRelation = changeRelation; }

    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }

    public void setContent(String content) { this.content = content; }

    public void setCommit(String commit) { this.commit = commit; }

    public void setDiff(JSONObject diff) { this.diff = diff; }

    public void setMethodBegin(int methodBegin) { this.methodBegin = methodBegin; }

    public String getCommit() { return commit; }

    public JSONObject getDiff() { return diff; }

    public void setMethodEnd(int methodEnd) { this.methodEnd = methodEnd; }

    public String getParentCommit() {
        return parentCommit;
    }

    public void setParentCommit(String parentCommit) {
        this.parentCommit = parentCommit;
    }
}
