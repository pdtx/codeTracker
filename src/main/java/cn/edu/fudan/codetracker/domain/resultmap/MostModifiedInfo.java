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
    private String methodName;
    private String packageName;
    private String moduleName;
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

    public String getMethodName() { return methodName; }

    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getPackageName() { return packageName; }

    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getModuleName() { return moduleName; }

    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}