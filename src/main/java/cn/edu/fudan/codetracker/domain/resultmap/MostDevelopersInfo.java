package cn.edu.fudan.codetracker.domain.resultmap;

public class MostDevelopersInfo {
    private String uuid;
    private String name;
    private String filePath;
    private int quantity;

    MostDevelopersInfo () {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
