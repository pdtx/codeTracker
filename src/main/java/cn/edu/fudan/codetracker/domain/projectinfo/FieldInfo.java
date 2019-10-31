/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:50
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import java.util.UUID;

public class FieldInfo {

    private String uuid;
    private String simpleName;
    private String modifier;
    private String simpleType;
    private String initValue;
    private String classUuid;
    private String packageUuid;


    private String moduleName;
    private String packageName;
    private String fileName;
    private String className;
    private String filePath;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    public FieldInfo(String simpleName, String modifier, String simpleType, String classUuid, String packageUuid, String moduleName, String packageName,
                     String fileName, String filePath, String className) {
        uuid = UUID.randomUUID().toString();
        this.simpleName = simpleName;
        this.modifier = modifier;
        this.simpleType = simpleType;
        this.classUuid = classUuid;
        this.packageUuid = packageUuid;
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.className = className;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if(obj == null){
            return false;
        }

        if(obj instanceof FieldInfo){
            FieldInfo fieldInfo = (FieldInfo) obj;
            return this.simpleName.equals(fieldInfo.getSimpleName()) &&
                    this.modifier.equals(fieldInfo.getModifier()) &&
                    this.simpleType.equals(fieldInfo.getSimpleType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * getter and setter
     * */

    public String getClassUuid() {
        return classUuid;
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

    public void setTrackerInfo(TrackerInfo trackerInfo) {
        this.trackerInfo = trackerInfo;
    }

    public void setTrackerInfo(String changeRelation, int version, String uuid) {
        trackerInfo = new TrackerInfo(changeRelation, version, uuid);
    }

    public String getInitValue() {
        return initValue;
    }

    public void setInitValue(String initValue) {
        this.initValue = initValue;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getSimpleType() {
        return simpleType;
    }

    public void setSimpleType(String simpleType) {
        this.simpleType = simpleType;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getPackageUuid() {
        return packageUuid;
    }

    public void setPackageUuid(String packageUuid) {
        this.packageUuid = packageUuid;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
}