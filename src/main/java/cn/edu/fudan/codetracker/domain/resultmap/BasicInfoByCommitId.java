package cn.edu.fudan.codetracker.domain.resultmap;

public class BasicInfoByCommitId {
    private String name;
    private String filePath;

    BasicInfoByCommitId() {

    }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFilePath() { return filePath; }

    public void setName(String name) { this.name = name; }

    public String getName() { return name; }
}
