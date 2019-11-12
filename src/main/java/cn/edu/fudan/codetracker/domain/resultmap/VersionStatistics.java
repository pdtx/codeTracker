/**
 * @description:
 * @author: fancying
 * @create: 2019-11-11 21:15
 **/
package cn.edu.fudan.codetracker.domain.resultmap;

public class VersionStatistics {

    private String uuid;
    private String name;
    private String filePath;

    private int version;
    private int quantity;

    VersionStatistics() {

    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
}