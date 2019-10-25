/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:48
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectInfo.MethodInfo;
import cn.edu.fudan.codetracker.domain.projectInfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.MethodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class MethodDao {

    private MethodMapper methodMapper;

    public void insertMethodInfoList(List<MethodInfo> methodInfos) {
        methodMapper.insertMethodInfoList(methodInfos);
    }

    public void insertRawMethodInfoList(List<MethodInfo> methodInfos) {
        methodMapper.insertRawMethodInfoList(methodInfos);
    }

    @Autowired
    public void setMethodMapper(MethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    public TrackerInfo getTrackerInfo(String filePath, String className, String signature) {
        return methodMapper.getTrackerInfo( filePath, className, signature);
    }

    public void setAddInfo(Set<MethodInfo> methodInfos) {
        if (methodInfos.isEmpty())
            return;
        List<MethodInfo> methodInfoArrayList = new ArrayList<>(methodInfos);
        insertMethodInfoList(methodInfoArrayList);
        insertRawMethodInfoList(methodInfoArrayList);
    }

    public void setDeleteInfo(Set<MethodInfo> methodInfos) {
        if (methodInfos.isEmpty())
            return;
        List<MethodInfo> methodInfoArrayList = new ArrayList<>(methodInfos);
        //methodMapper.setDeleteInfo(methodInfoArrayList);
        insertRawMethodInfoList(methodInfoArrayList);
    }

    public void setChangeInfo(Set<MethodInfo> methodInfos) {
        if (methodInfos.isEmpty())
            return;
        List<MethodInfo> methodInfoArrayList = new ArrayList<>(methodInfos);
        methodMapper.updateChangeInfo(methodInfoArrayList);
        insertRawMethodInfoList(methodInfoArrayList);
    }

    public Object getMethodHistory(String repoId, String moduleName, String packageName, String className, String signature) {
        return  methodMapper.getMethodHistory(repoId, moduleName, packageName, className, signature);
    }
}