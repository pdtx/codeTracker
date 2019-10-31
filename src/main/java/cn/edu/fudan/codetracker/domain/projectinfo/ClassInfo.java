/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:38
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import java.util.*;

public class ClassInfo {

    private String uuid;
    private String fullname;
    private String className;
    private String fileName;
    private String filePath;
    private String packageName;
    private String moduleName;
    private String fileUuid;
    private String packageUuid;
    private String modifier;

    private CommonInfo commonInfo;
    private TrackerInfo trackerInfo;



    private int begin;
    private int end;
    private List<String> extendedList;
    private List<String> implementedList;
    private List<FieldInfo> fieldInfos;
    private List<MethodInfo> methodInfos;

    public ClassInfo(String fullname, String className, String filePath, String fileName, String packageName,
                     String moduleName, String fileUuid, String packageUuid, String modifier, int begin, int end) {
        uuid = UUID.randomUUID().toString();
        this.fullname = fullname;
        this.className = className;
        this.filePath = filePath;
        this.fileName = fileName;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.fileUuid = fileUuid;
        this.packageUuid = packageUuid;
        this.modifier = modifier;
        this.begin = begin;
        this.end = end;
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public List<String> getExtendedList() {
        return extendedList;
    }

    public void setExtendedList(List<String> extendedList) {
        this.extendedList = extendedList;
    }

    public List<String> getImplementedList() {
        return implementedList;
    }

    public void setImplementedList(List<String> implementedList) {
        this.implementedList = implementedList;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public void setFieldInfos(List<FieldInfo> filedInfos) {
        this.fieldInfos = filedInfos;
    }

    public List<MethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public void setMethodInfos(List<MethodInfo> methodInfos) {
        this.methodInfos = methodInfos;
    }

    private String asString(List<String> stringList) {
        StringBuilder sb = new StringBuilder();
        for (String string : stringList) {
            sb.append(string);
            sb.append(" ");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return (filePath + className).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof ClassInfo){
            ClassInfo classInfo = (ClassInfo) o;
            return this.uuid.equals(classInfo.uuid);
        }
        return false;
    }

    /**
     * getter and setter
     * */
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPackageUuid() {
        return packageUuid;
    }

    public void setPackageUuid(String packageUuid) {
        this.packageUuid = packageUuid;
    }
}