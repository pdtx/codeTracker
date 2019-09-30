/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:51
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.*;

public class MethodInfo {

    private String uuid;
    private String fullname;
    private String signature;
    private String content;

    private String className;
    private String classUuid;
    private String fileName;
    private String packageName;
    private String packageUuid;
    private String moduleName;


    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    private String modifier;
    private String primitiveType;
    private int begin;
    private int end;
    private List<StatementInfo> statementInfos;

    public MethodInfo(String className, String classUuid, String fileName, String packageName, String packageUuid, String moduleName) {
        uuid = UUID.randomUUID().toString();
        this.className = className;
        this.classUuid = classUuid;
        this.fileName = fileName;
        this.packageName = packageName;
        this.packageUuid = packageUuid;
        this.moduleName = moduleName;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassUuid() {
        return classUuid;
    }

    public void setClassUuid(String classUuid) {
        this.classUuid = classUuid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public List<StatementInfo> getStatementInfos() {
        return statementInfos;
    }

    public void setStatementInfos(List<StatementInfo> statementInfos) {
        this.statementInfos = statementInfos;
    }


    public void setTrackerInfo(String changeRelation, int version, String uuid) {
        trackerInfo = new TrackerInfo(changeRelation, version, uuid);
    }
}