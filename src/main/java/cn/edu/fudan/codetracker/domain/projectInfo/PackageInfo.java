/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 20:37
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.*;

public class PackageInfo {

    private String uuid;
    private String packageName;
    private String moduleName;
    private List<ClassInfo> classInfos; // ?????

    private String startCommit;
    private String endCommit;
    private String repoUuid;
    private String branch;
    private Date startCommitDate;
    private Date endCommitDate;




    public PackageInfo(String moduleName, String packageName, List<ClassInfo> classInfos) {
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.classInfos = classInfos;
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

    public List<ClassInfo> getClassInfos() {
        return classInfos;
    }

    public void setClassInfos(List<ClassInfo> classInfos) {
        this.classInfos = classInfos;
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
        return 0;
    }
}