/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 17:08
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.PackageInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.PackageMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class PackageDao {

    private PackageMapper packageMapper;

    @Autowired
    public void setPackageMapper(PackageMapper packageMapper) {
        this.packageMapper = packageMapper;
    }

    public void insertPackageInfoList(List<PackageInfo> packageInfos) {
        packageMapper.insertPackageInfoList(packageInfos);
    }

    public void insertRawPackageInfoList(List<PackageInfo> packageInfos) {
        packageMapper.insertRawPackageInfoList(packageInfos);
    }


    public TrackerInfo getTrackerInfo(String moduleName, String packageName, String repoUuid, String branch) {
        return packageMapper.getTrackerInfo(moduleName, packageName, repoUuid, branch);
    }

    public void setAddInfo(@NotNull Set<PackageInfo> packageInfos) {
        if (packageInfos.size() == 0) {
            return;
        }
        List<PackageInfo> packageInfoList = new ArrayList<>(packageInfos);
        insertPackageInfoList(packageInfoList);
        insertRawPackageInfoList(packageInfoList);
    }

    public void setDeleteInfo(@NotNull Set<PackageInfo> packageInfos) {
        if (packageInfos.size() == 0) {
            return;
        }
        List<PackageInfo> packageInfoList = new ArrayList<>(packageInfos);
        //packageMapper.updateDeleteInfo(packageInfoList);
        insertRawPackageInfoList(packageInfoList);
    }

    public void setChangeInfo(@NotNull Set<PackageInfo> packageInfos) {
        if (packageInfos.size() == 0) {
            return;
        }
        List<PackageInfo> packageInfoList = new ArrayList<>(packageInfos);
        packageMapper.updateChangeInfo(packageInfoList);
        insertRawPackageInfoList(packageInfoList);
    }

}