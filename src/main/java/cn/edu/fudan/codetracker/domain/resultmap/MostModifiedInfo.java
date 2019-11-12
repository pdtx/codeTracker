/**
 * @description:
 * @author: fancying
 * @create: 2019-11-12 11:00
 **/
package cn.edu.fudan.codetracker.domain.resultmap;

public class MostModifiedInfo {
    private String name;
    private String className;
    private String filePath;
    private int version;

    MostModifiedInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}