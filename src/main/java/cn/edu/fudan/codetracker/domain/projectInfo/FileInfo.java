/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 17:19
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.UUID;

public class FileInfo {


    private String uuid;
    private String fileName;
    private String filePath;
    private String packageName;
    private String moduleName;

    // 指示属于哪个版本的package，与raw_package的UUID字段关联
    private String packageUuid;

    private TrackerInfo trackerInfo;
    private CommonInfo commonInfo;
    


    public FileInfo(String fileName, String filePath, String packageName, String moduleName, String packageUuid) {
        uuid = UUID.randomUUID().toString();
        this.fileName = fileName;
        this.filePath = filePath;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.packageUuid = packageUuid;

    }

/*    public FileInfo(String fileName, String packageName, String moduleName, String filePath, String) {
        uuid = UUID.randomUUID().toString();
        this.fileName = fileName;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.filePath = filePath;
    }*/

    // filePath 可以唯一指定一个file
    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    /**
     * getter and setter
     * */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getmoduleName() {
        return moduleName;
    }

    public void setmoduleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPackageUuid() {
        return packageUuid;
    }

    public void setPackageUuid(String packageUuid) {
        this.packageUuid = packageUuid;
    }

    public CommonInfo getCommonInfo() {
        return commonInfo;
    }

    public void setCommonInfo(CommonInfo commonInfo) {
        this.commonInfo = commonInfo;
    }

    public void setTrackerInfo(String changeRelation, int version, String uuid) {
        trackerInfo = new TrackerInfo(changeRelation, version, uuid);
    }

    public TrackerInfo getTrackerInfo() {
        return trackerInfo;
    }

    public void setTrackerInfo(TrackerInfo trackerInfo) {
        this.trackerInfo = trackerInfo;
    }
}