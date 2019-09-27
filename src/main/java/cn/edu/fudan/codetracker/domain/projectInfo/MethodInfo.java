/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:51
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.*;

public class MethodInfo {

    private String uuid;

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    private String fullname;
    private String signature;
    private String className;
    private String fileName;
    private String packageName;
    private String moduleName;
    private String classUuid;
    private String packageUuid;
    private String content;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;

    private String modifier;
    private String primitiveType;
    private int begin;
    private int end;
    private List<StatementInfo> statementInfos;

    public MethodInfo(String className, String classUuid, String packageName) {
        uuid = UUID.randomUUID().toString();
        this.className = className;
        this.classUuid = classUuid;
        this.packageName = packageName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
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

    public List<StatementInfo> getStatementInfo() {
        return statementInfos;
    }

    public void setStatementInfo(List<StatementInfo> statementInfos) {
        this.statementInfos = statementInfos;
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
}