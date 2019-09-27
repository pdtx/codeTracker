/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 21:51
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.Date;

public class CommonInfo {

    private String startCommit;
    private Date startCommitDate ;
    private String endCommit;
    private Date endCommitDate ;
    private String repoUuid;
    private String branch;

    public CommonInfo(String startCommit, Date startCommitDate, String endCommit, Date endCommitDate, String repoUuid, String branch) {
        this.startCommit = startCommit;
        this.startCommitDate = startCommitDate;
        this.endCommit = endCommit;
        this.endCommitDate = endCommitDate;
        this.repoUuid = repoUuid;
        this.branch = branch;
    }

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

}