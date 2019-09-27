/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 21:50
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.Date;

public class TrackerInfo {



    private Date commitDate ;
    private String commit;
    private String committer;
    private String commitMessage;
    private String parentCommit;

    // 根据具体情况 单独获取
    private int version;
    private String changeRelation;
    private String rootUUID;

    public TrackerInfo( Date commitDate, String commit, String committer, String commitMessage, String parentCommit) {
        this.commitDate = commitDate;
        this.commit = commit;
        this.committer = committer;
        this.commitMessage = commitMessage;
        this.parentCommit = parentCommit;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setRootUUID(String rootUUID) {
        this.rootUUID = rootUUID;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public String getCommit() {
        return commit;
    }

    public String getCommitter() {
        return committer;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public String getParentCommit() {
        return parentCommit;
    }

    public String getChangeRelation() {
        return changeRelation;
    }

    public String getRootUUID() {
        return rootUUID;
    }

    public void setChangeRelation(String changeRelation) {
        this.changeRelation = changeRelation;
    }
}