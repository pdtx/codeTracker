/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 17:08
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectInfo.PackageInfo;
import cn.edu.fudan.codetracker.mapper.PackageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}