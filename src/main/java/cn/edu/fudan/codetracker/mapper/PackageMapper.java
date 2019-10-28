package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.PackageInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageMapper {

    void insertPackageInfoList(List<PackageInfo> packageInfos);

    void insertRawPackageInfoList(List<PackageInfo> packageInfos);

    TrackerInfo getTrackerInfo(String moduleName, String packageName);

    void updateDeleteInfo(List<PackageInfo> packageInfoList);

    void updateChangeInfo(List<PackageInfo> packageInfoList);
}
