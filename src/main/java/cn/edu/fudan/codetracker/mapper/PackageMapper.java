package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectInfo.PackageInfo;
import cn.edu.fudan.codetracker.domain.projectInfo.TrackerInfo;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Repository
public interface PackageMapper {

    void insertPackageInfoList(List<PackageInfo> packageInfos);

    void insertRawPackageInfoList(List<PackageInfo> packageInfos);

    TrackerInfo getTrackerInfo(String moduleName, String packageName);

    void updateDeleteInfo(List<PackageInfo> packageInfoList);

    void updateChangeInfo(List<PackageInfo> packageInfoList);
}
