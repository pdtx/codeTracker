/**
 * @description:
 * @author: fancying
 * @create: 2019-11-25 11:12
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

public class CodeLine {

    private String commitId;
    private String committer;
    private String commitDate;

    private int addLines;
    private int deleteLines;
    private int changeLines;

    public CodeLine() {

    }

    /**
     * getter and setter
     */
    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public int getAddLines() {
        return addLines;
    }

    public void setAddLines(int addLines) {
        this.addLines = addLines;
    }

    public int getDeleteLines() {
        return deleteLines;
    }

    public void setDeleteLines(int deleteLines) {
        this.deleteLines = deleteLines;
    }

    public int getChangeLines() {
        return changeLines;
    }

    public void setChangeLines(int changeLines) {
        this.changeLines = changeLines;
    }

}