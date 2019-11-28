/**
 * @description: super class of all project info class
 * @author: fancying
 * @create: 2019-11-11 20:18
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;

import java.util.Date;
import java.util.List;

public class BaseInfo {

    private String startCommit;
    private Date startCommitDate ;
    private String endCommit;
    private Date endCommitDate ;

    private String repoUuid;
    private String branch;
    private Date commitDate ;
    private String commit;
    private String committer;
    private String commitMessage;
    /**
     * just parent commit not previous version commit
     */
    private String parentCommit;

    /**
     * tree parent
     */
    private BaseInfo parent;
    private List<? extends BaseInfo> children;
    private ProjectInfoLevel projectInfoLevel;

    BaseInfo() { }

    public BaseInfo(String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit) {
        this.repoUuid = repoUuid;
        this.branch = branch;
        this.commitDate = commitDate;
        this.commit = commit;
        this.committer = committer;
        this.commitMessage = commitMessage;
        this.parentCommit = parentCommit;

        this.startCommit = commit;
        this.endCommit = commit;
        this.startCommitDate = commitDate;
        this.endCommitDate = commitDate;
    }

    public BaseInfo(BaseInfo baseInfo) {
        this.repoUuid = baseInfo.getRepoUuid();
        this.branch = baseInfo.getBranch();
        this.commitDate = baseInfo.getCommitDate();
        this.commit = baseInfo.getCommit();
        this.committer = baseInfo.getCommitter();
        this.commitMessage = baseInfo.getCommitMessage();
        this.parentCommit = baseInfo.getParentCommit();

        this.startCommit = commit;
        this.endCommit = commit;
        this.startCommitDate = commitDate;
        this.endCommitDate = commitDate;
    }


    /**
     * getter and setter
     */
    public String getStartCommit() {
        return startCommit;
    }

    public void setStartCommit(String startCommit) {
        this.startCommit = startCommit;
    }

    public Date getStartCommitDate() {
        return startCommitDate;
    }

    public void setStartCommitDate(Date startCommitDate) {
        this.startCommitDate = startCommitDate;
    }

    public String getEndCommit() {
        return endCommit;
    }

    public void setEndCommit(String endCommit) {
        this.endCommit = endCommit;
    }

    public Date getEndCommitDate() {
        return endCommitDate;
    }

    public void setEndCommitDate(Date endCommitDate) {
        this.endCommitDate = endCommitDate;
    }

    public String getRepoUuid() {
        return repoUuid;
    }

    public void setRepoUuid(String repoUuid) {
        this.repoUuid = repoUuid;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public BaseInfo getParent() {
        return parent;
    }

    public void setParent(BaseInfo parent) {
        this.parent = parent;
    }

    public ProjectInfoLevel getProjectInfoLevel() {
        return projectInfoLevel;
    }

    public void setProjectInfoLevel(ProjectInfoLevel projectInfoLevel) {
        this.projectInfoLevel = projectInfoLevel;
    }

    public String getParentCommit() {
        return parentCommit;
    }

    public void setParentCommit(String parentCommit) {
        this.parentCommit = parentCommit;
    }

    public List<? extends BaseInfo> getChildren() {
        return children;
    }

    public void setChildren(List<? extends BaseInfo> children) {
        this.children = children;
    }
}