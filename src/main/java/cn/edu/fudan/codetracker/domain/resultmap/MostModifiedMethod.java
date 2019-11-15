package cn.edu.fudan.codetracker.domain.resultmap;

public class MostModifiedMethod {
    private String uuid;
    private String methodName;
    private String className;
    private String filePath;
    private int version;

    MostModifiedMethod() {

    }

    public int getVersion() { return version; }

    public void setVersion(int version) { this.version = version; }

    public String getClassName() { return className; }

    public void setClassName(String className) { this.className = className; }

    public String getFilePath() { return filePath; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getMethodName() { return methodName; }

    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }
}
