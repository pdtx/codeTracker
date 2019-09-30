/**
 * @description:
 * @author: fancying
 * @create: 2019-06-06 17:19
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.Date;
import java.util.UUID;

public class FileInfo {


    private String uuid;
    private String fileName;
    private String path;
    private String packageName;
    private String moduleName;

    private String packageUuid;

    private TrackerInfo trackerInfo;
    private CommonInfo commonInfo;
    


    public FileInfo(String fileName, String path, String packageName, String moduleName, String packageUuid) {
        uuid = UUID.randomUUID().toString();
        this.fileName = fileName;
        this.path = path;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.packageUuid = packageUuid;

    }

    public FileInfo(String fileName, String packageName, String moduleName, String path) {
        uuid = UUID.randomUUID().toString();
        this.fileName = fileName;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public String getpackageUuid() {
        return packageUuid;
    }

    public void setpackageUuid(String packageUuid) {
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
}