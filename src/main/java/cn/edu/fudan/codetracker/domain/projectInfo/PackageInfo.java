/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:37
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.*;

public class PackageInfo {

    private String uuid;
    private String packageName;
    private String moduleName;
    private List<ClassInfo> classInfos; // ?????

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    // 根据具体情况 单独获取
    private int version;
    private String changeRelation;
    private String rootUUID;



    public PackageInfo(String moduleName, String packageName, List<ClassInfo> classInfos) {
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.classInfos = classInfos;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(moduleName);
        sb.append(packageName);
        char[] charArray = sb.toString().toCharArray();
        int hash = 0;
        for (char c : charArray) {
            hash = hash * 131 + c;
        }
        return 0;
    }


    /**
     * getter and setter
     * */
    public void setTrackerInfo(TrackerInfo trackerInfo) {
        this.trackerInfo = trackerInfo;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<ClassInfo> getClassInfos() {
        return classInfos;
    }

    public void setClassInfos(List<ClassInfo> classInfos) {
        this.classInfos = classInfos;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public CommonInfo getCommonInfo() {
        return commonInfo;
    }

    public void setCommonInfo(CommonInfo commonInfo) {
        this.commonInfo = commonInfo;
    }

    public TrackerInfo getTrackerInfo() {
        return trackerInfo;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getChangeRelation() {
        return changeRelation;
    }

    public void setChangeRelation(String changeRelation) {
        this.changeRelation = changeRelation;
    }

    public String getRootUUID() {
        return rootUUID;
    }

    public void setRootUUID(String rootUUID) {
        this.rootUUID = rootUUID;
    }

    public void setTrackerInfo(String changeRelation, int version, String uuid) {
        trackerInfo = new TrackerInfo(changeRelation, version, uuid);
    }
}