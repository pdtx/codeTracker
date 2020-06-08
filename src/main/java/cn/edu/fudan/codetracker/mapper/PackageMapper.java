package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.PackageNode;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageMapper {

    void insertPackageInfoList(@Param("packageNodes") List<PackageNode> packageNodes, @Param("commonInfo")CommonInfo commonInfo);

    void insertRawPackageInfoList(@Param("packageNodes") List<PackageNode> packageNodes, @Param("commonInfo")CommonInfo commonInfo);

    void updateChangeInfo(@Param("packageNodes") List<PackageNode> packageNodes, @Param("commonInfo")CommonInfo commonInfo);



    TrackerInfo getTrackerInfo(@Param("moduleName") String moduleName, @Param("packageName") String packageName, @Param("repoUuid") String repoUuid, @Param("branch") String branch);

    /**
     * 判断某个repo的某个branch是否扫描过
     */
    String findScanLatest(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
}
