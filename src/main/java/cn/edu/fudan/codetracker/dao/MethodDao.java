/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:48
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.MethodInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.MethodNode;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
import cn.edu.fudan.codetracker.mapper.MethodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class MethodDao {

    private MethodMapper methodMapper;

    @Autowired
    public void setMethodMapper(MethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }

    public void insertMethodInfoList(List<MethodNode> methodNodes, CommonInfo commonInfo) {
        methodMapper.insertMethodInfoList(methodNodes, commonInfo);
    }

    public void insertRawMethodInfoList(List<MethodNode> methodNodes, CommonInfo commonInfo) {
        methodMapper.insertRawMethodInfoList(methodNodes, commonInfo);
    }

    public void setAddInfo(Set<MethodNode> methodNodes, CommonInfo commonInfo) {
        if (methodNodes.isEmpty()) {
            return;
        }
        List<MethodNode> methodInfoArrayList = new ArrayList<>(methodNodes);
        insertMethodInfoList(methodInfoArrayList, commonInfo);
        insertRawMethodInfoList(methodInfoArrayList, commonInfo);
    }

    public void setDeleteInfo(Set<MethodNode> methodNodes, CommonInfo commonInfo) {
        if (methodNodes.isEmpty()) {
            return;
        }
        List<MethodNode> methodInfoArrayList = new ArrayList<>(methodNodes);
        insertRawMethodInfoList(methodInfoArrayList, commonInfo);
    }

    public void setChangeInfo(Set<MethodNode> methodNodes, CommonInfo commonInfo) {
        if (methodNodes.isEmpty()) {
            return;
        }
        List<MethodNode> methodInfoArrayList = new ArrayList<>(methodNodes);
        methodMapper.updateChangeInfo(methodInfoArrayList, commonInfo);
        insertRawMethodInfoList(methodInfoArrayList, commonInfo);
    }

    public TrackerInfo getTrackerInfo(String filePath, String className, String signature, String repoUuid, String branch) {
        return methodMapper.getTrackerInfo( filePath, className, signature, repoUuid, branch);
    }
}