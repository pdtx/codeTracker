package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.ScanInfo;
import cn.edu.fudan.codetracker.mapper.RepoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepoDao {
    private RepoMapper repoMapper;

    @Autowired
    public void setRepoMapper(RepoMapper repoMapper) {
        this.repoMapper = repoMapper;
    }

    public void insertScanRepo(ScanInfo scanInfo) {
        repoMapper.insertScanRepo(scanInfo);
    }

    public void updateScanInfo(ScanInfo scanInfo) {
        repoMapper.updateScanInfo(scanInfo);
    }

    public void saveScanInfo(ScanInfo scanInfo) {
        repoMapper.saveScanInfo(scanInfo);
    }

    public ScanInfo getScanInfo(String repoId) {
        return repoMapper.getScanInfo(repoId);
    }

}
