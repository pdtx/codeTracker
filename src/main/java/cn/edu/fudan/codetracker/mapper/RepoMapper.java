package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.ScanInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoMapper {

    void insertScanRepo(@Param("scanInfo")ScanInfo scanInfo);

    /**
     * 扫描过程更新最新commit和已扫commit数量
     * @param scanInfo
     */
    void updateScanInfo(@Param("scanInfo")ScanInfo scanInfo);

    /**
     * 扫描结束或中止 更新扫描状态
     * @param scanInfo
     */
    void saveScanInfo(@Param("scanInfo")ScanInfo scanInfo);

    ScanInfo getScanInfo(@Param("repoId") String repoId);
}
