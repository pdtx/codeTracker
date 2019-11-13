package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.MethodInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MethodMapper {

    void insertMethodInfoList(List<MethodInfo> methodInfos);

    void insertRawMethodInfoList(List<MethodInfo> methodInfos);

    TrackerInfo getTrackerInfo(@Param("filePath") String filePath, @Param("className") String className, @Param("signature") String signature, @Param("repoUuid") String repoUuid, @Param("branch") String branch);

    void updateChangeInfo(List<MethodInfo> methodInfoArrayList);

    List<MethodInfo> getMethodHistory(@Param("repoId") String repoId, @Param("moduleName") String moduleName, @Param("packageName") String packageName, @Param("className") String className, @Param("signature") String signature);

    List<VersionStatistics> getMethodStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
}
