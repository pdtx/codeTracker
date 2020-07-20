
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.PackageNode;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.PackageMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 * description:
 * @author fancying
 * create: 2019-09-26 17:08
 **/
@Repository
public class PackageDao {

    private PackageMapper packageMapper;

    @Autowired
    public void setPackageMapper(PackageMapper packageMapper) {
        this.packageMapper = packageMapper;
    }

    public void insertPackageInfoList(List<PackageNode> packageNodes, CommonInfo commonInfo) {
        if (packageNodes.size() == 0) {
            return;
        }
        packageMapper.insertPackageInfoList(packageNodes, commonInfo);
    }

    public void insertRawPackageInfoList(List<PackageNode> packageNodes, CommonInfo commonInfo) {
        if (packageNodes.size() == 0) {
            return;
        }
        packageMapper.insertRawPackageInfoList(packageNodes, commonInfo);
    }

    public void setAddInfo(@NotNull Set<PackageNode> packageNodes, CommonInfo commonInfo) {
        if (packageNodes.size() == 0) {
            return;
        }
        List<PackageNode> packageInfoList = new ArrayList<>(packageNodes);
        insertPackageInfoList(packageInfoList, commonInfo);
        insertRawPackageInfoList(packageInfoList, commonInfo);
    }

    public void setDeleteInfo(@NotNull Set<PackageNode> packageNodes, CommonInfo commonInfo) {
        if (packageNodes.size() == 0) {
            return;
        }
        List<PackageNode> packageInfoList = new ArrayList<>(packageNodes);
        insertRawPackageInfoList(packageInfoList, commonInfo);
    }

    public void setChangeInfo(@NotNull Set<PackageNode> packageNodes, CommonInfo commonInfo) {
        if (packageNodes.size() == 0) {
            return;
        }
        List<PackageNode> packageInfoList = new ArrayList<>(packageNodes);
        packageMapper.updateChangeInfo(packageInfoList, commonInfo);
        insertRawPackageInfoList(packageInfoList, commonInfo);
    }


    public TrackerInfo getTrackerInfo(String moduleName, String packageName, String repoUuid, String branch) {
        return packageMapper.getTrackerInfo(moduleName, packageName, repoUuid, branch);
    }

    /**
     * 判断某个repo的某个branch是否扫描过
     */
    public String findScanLatest(String repoUuid, String branch) {
        return packageMapper.findScanLatest(repoUuid, branch);
    }

}