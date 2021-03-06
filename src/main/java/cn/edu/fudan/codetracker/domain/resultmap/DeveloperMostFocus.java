package cn.edu.fudan.codetracker.domain.resultmap;

import java.util.List;

public class DeveloperMostFocus {
    private String repo;
    private String branch;
    private String uuid;
    private String name;
    private int quantity;
    private String filePath;
    private List<String> content;

    public DeveloperMostFocus() {

    }

    public String getRepo() { return repo; }

    public void setRepo(String repo) { this.repo = repo; }

    public String getBranch() { return branch; }

    public void setBranch(String branch) { this.branch = branch; }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getFilePath() { return filePath; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public List<String> getContent() { return content; }

    public void setContent(List<String> content) { this.content = content; }
}
