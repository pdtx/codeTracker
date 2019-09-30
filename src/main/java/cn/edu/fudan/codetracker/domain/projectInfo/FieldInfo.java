/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:50
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FieldInfo {

    private String uuid;
    private String simpleName;
    private String modifier;
    private String simpleType;
    private String initValue;
    private String classUuid;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    public FieldInfo(String simpleName, String modifier, String simpleType, String classUuid) {
        uuid = UUID.randomUUID().toString();
        this.simpleName = simpleName;
        this.modifier = modifier;
        this.simpleType = simpleType;
        this.classUuid = classUuid;
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

/*    @Override
    public int hashCode() {
        return 10;
    }*/

    @Override
    public boolean equals(Object o) {
        FieldInfo obj = (FieldInfo) o;
        return this.simpleName.equals(obj.getSimpleName()) &&
                this.modifier.equals(obj.getModifier()) &&
                this.simpleType.equals(obj.getSimpleType());
    }


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
}