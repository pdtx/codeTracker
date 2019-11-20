/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:37
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;

import java.util.*;

public class PackageInfo extends BaseInfo{

    private String uuid;
    private String packageName;
    private String moduleName;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    // 根据具体情况 单独获取

    public PackageInfo() {

    }

    public PackageInfo(String moduleName, String packageName) {
        super();
        this.moduleName = moduleName;
        this.packageName = packageName;
    }

    public PackageInfo(BaseInfo baseInfo, List<FileInfo> children, String moduleName, String packageName) {
        super(baseInfo);
        super.setParent(null);
        super.setChildren(children);
        super.setProjectInfoLevel(ProjectInfoLevel.PACKAGE);
        this.moduleName = moduleName;
        this.packageName = packageName;
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
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof PackageInfo){
            PackageInfo packageInfo = (PackageInfo) o;
            return this.uuid.equals(packageInfo.uuid);
        }
        return false;
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


    public void setTrackerInfo(String changeRelation, int version, String uuid) {
        trackerInfo = new TrackerInfo(changeRelation, version, uuid);
    }
}