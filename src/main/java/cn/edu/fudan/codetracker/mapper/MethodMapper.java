package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.MethodInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MethodMapper {

    void insertMethodInfoList(List<MethodInfo> methodInfos);

    void insertRawMethodInfoList(List<MethodInfo> methodInfos);

    TrackerInfo getTrackerInfo(String filePath, String className, String signature, String repoUuid, String branch);

    void updateChangeInfo(List<MethodInfo> methodInfoArrayList);

    List<MethodInfo> getMethodHistory(String repoId, String moduleName, String packageName, String className, String signature);

    List<VersionStatistics> getMethodStatistics(String repoUuid, String branch);
}
