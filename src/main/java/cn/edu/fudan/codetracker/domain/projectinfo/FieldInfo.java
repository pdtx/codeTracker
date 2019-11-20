/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:50
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FieldInfo extends BaseInfo{

    private String uuid;
    private String fullName;
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
                          String fileName, String filePath, String className, String initValue) {
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
        this.initValue = initValue;
    }

    public FieldInfo(BaseInfo baseInfo, List<StatementInfo> children, ClassInfo parent,
                     String simpleName, String modifier, String simpleType, String initValue) {
        super(baseInfo);
        super.setParent(parent);
        super.setChildren(children);
        super.setProjectInfoLevel(ProjectInfoLevel.FIELD);

        uuid = UUID.randomUUID().toString();
        this.simpleName = simpleName;
        this.modifier = modifier;
        this.simpleType = simpleType;
        this.initValue = initValue;

        this.classUuid = parent.getUuid();
        this.packageUuid = parent.getPackageUuid();
        this.moduleName = parent.getModuleName();
        this.packageName = parent.getClassName();
        this.fileName = parent.getFileName();
        this.filePath = parent.getFilePath();
        this.className = parent.getClassName();
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}