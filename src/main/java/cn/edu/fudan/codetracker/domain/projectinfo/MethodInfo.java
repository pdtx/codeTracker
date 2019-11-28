/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:51
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.RelationShip;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class MethodInfo extends BaseInfo{

    private String uuid;
    private String simpleName;
    private String fullname;
    private String signature;
    private String content;
    /**
     * filePath eg: scan-service/src/main/java/cn/edu/fudan/scanservice/tools/FindBugScanOperation.java
     */
    private TrackerInfo trackerInfo;

    private String modifier;
    private String primitiveType;
    private int begin;
    private int end;
    private JSONObject diff;

    // mybatis 无需这种构造函数 只需要无参构造函数就行
    // 反射机制需要调用类的无参构造函数
    // mybatis 通过反射实现数据注入
    public MethodInfo() {
    }

    public MethodInfo(BaseInfo baseInfo, ClassInfo parent) {
        super(baseInfo);
        super.setParent(parent);
        super.setProjectInfoLevel(ProjectInfoLevel.METHOD);

        uuid = UUID.randomUUID().toString();
        diff = new JSONObject();
        diff.put("data",new JSONArray());
        trackerInfo =  new TrackerInfo(RelationShip.ADD.name(), 1, uuid);
    }

    @Override
    public int hashCode() {
        ClassInfo classInfo = (ClassInfo) super.getParent();
        return (classInfo.getFilePath() + classInfo.getClassName() + signature).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof MethodInfo){
            MethodInfo methodInfo = (MethodInfo) o;
            return this.uuid.equals(methodInfo.uuid);
        }
        return false;
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

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TrackerInfo getTrackerInfo() {
        return trackerInfo;
    }

    public void setTrackerInfo(TrackerInfo trackerInfo) {
        this.trackerInfo = trackerInfo;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(String primitiveType) {
        this.primitiveType = primitiveType;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setTrackerInfo(String changeRelation, int version, String uuid) {
        trackerInfo = new TrackerInfo(changeRelation, version, uuid);
    }

    public JSONObject getDiff() {
        return diff;
    }

    public void setDiff(JSONObject diff) {
        this.diff = diff;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }
}