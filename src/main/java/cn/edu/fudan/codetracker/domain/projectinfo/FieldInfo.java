/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:50
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.RelationShip;

import java.util.UUID;

public class FieldInfo extends BaseInfo{

    private String uuid;
    private String fullName;
    private String simpleName;
    private String modifier;
    private String simpleType;
    private String initValue;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    public FieldInfo() {

    }

    public FieldInfo(BaseInfo baseInfo, ClassInfo parent, String simpleName, String modifier, String simpleType, String initValue) {
        super(baseInfo);
        super.setParent(parent);
        super.setProjectInfoLevel(ProjectInfoLevel.FIELD);

        uuid = UUID.randomUUID().toString();
        this.simpleName = simpleName;
        this.modifier = modifier;
        this.simpleType = simpleType;
        this.initValue = initValue;
        trackerInfo =  new TrackerInfo(RelationShip.ADD.name(), 1, uuid);
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}