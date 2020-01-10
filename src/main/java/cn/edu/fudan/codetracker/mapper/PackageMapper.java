package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.PackageInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageMapper {

    void insertPackageInfoList(List<PackageInfo> packageInfos);

    void insertRawPackageInfoList(List<PackageInfo> packageInfos);

    TrackerInfo getTrackerInfo(@Param("moduleName") String moduleName, @Param("packageName") String packageName, @Param("repoUuid") String repoUuid, @Param("branch") String branch);

    void updateDeleteInfo(List<PackageInfo> packageInfoList);

    void updateChangeInfo(List<PackageInfo> packageInfoList);

    /**
     * 判断某个repo的某个branch是否扫描过
     */
    String findScanLatest(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
}
