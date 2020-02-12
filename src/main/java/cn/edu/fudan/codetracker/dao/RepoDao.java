package cn.edu.fudan.codetracker.dao;

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

    public void insertScanRepo(String uuid, String repoId, String branch, String status) {
        repoMapper.insertScanRepo(uuid, repoId, branch, status);
    }

    public void updateScanStatus(String repoId, String branch, String status) {
        repoMapper.updateScanStatus(repoId, branch, status);
    }

    public void updateLatestCommit(String repoId, String branch, String latestCommit) {
        repoMapper.updateLatestCommit(repoId, branch, latestCommit);
    }

    public String getScanStatus(String repoId, String branch) {
        return repoMapper.getScanStatus(repoId, branch);
    }

    public String getLatestScan(String repoId, String branch) {
        return repoMapper.getLatestScan(repoId, branch);
    }
}
