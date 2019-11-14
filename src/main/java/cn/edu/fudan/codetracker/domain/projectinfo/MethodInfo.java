/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:51
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class MethodInfo {

    public MethodInfo() {
    }

     // mybatis 无需这种构造函数 只需要无参构造函数就行
    // 反射机制需要调用类的无参构造函数
    // mybatis 通过反射实现数据注入
/*    public MethodInfo(String uuid, String fullname, String signature, String content, String commit,
                      String committer, String commitMessage, java.sql.Timestamp commitDate, Integer version, String relation) {
        this.uuid = uuid;
        this.fullname = fullname;
        this.signature = signature;
        this.content = content;
        this.commonInfo = new CommonInfo(commit, committer, commitMessage, commitDate);
        this.trackerInfo = new TrackerInfo(version, relation);
    }*/

    private String uuid;
    private String simpleName;
    private String fullname;
    private String signature;
    private String content;

    private String classUuid;
    private String moduleName;
    private String packageName;
    private String fileName;
    // filePath eg: scan-service/src/main/java/cn/edu/fudan/scanservice/tools/FindBugScanOperation.java
    private String filePath;
    private String className;
    private String packageUuid;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    private String modifier;
    private String primitiveType;
    private int begin;
    private int end;
    private JSONObject diff;
    private List<StatementInfo> statementInfos;



    public MethodInfo(String className, String classUuid, String fileName, String filePath, String packageName, String packageUuid, String moduleName) {
        uuid = UUID.randomUUID().toString();
        this.className = className;
        this.classUuid = classUuid;
        this.fileName = fileName;
        this.filePath = filePath;
        this.packageName = packageName;
        this.packageUuid = packageUuid;
        this.moduleName = moduleName;
        diff = new JSONObject();
        diff.put("data",new JSONArray());
    }

    @Override
    public int hashCode() {
        return (filePath + className + signature).hashCode();
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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