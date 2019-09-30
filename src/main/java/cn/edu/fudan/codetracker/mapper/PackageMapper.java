package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectInfo.PackageInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageMapper {

    void insertPackageInfoList(List<PackageInfo> packageInfos);

    void insertRawPackageInfoList(List<PackageInfo> packageInfos);
}
