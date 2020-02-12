package cn.edu.fudan.codetracker.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoMapper {

    void insertScanRepo(@Param("uuid") String uuid, @Param("repoId") String repoId, @Param("branch") String branch, @Param("status") String status);

    void updateScanStatus(@Param("repoId") String repoId, @Param("branch") String branch, @Param("status") String status);

    void updateLatestCommit(@Param("repoId") String repoId, @Param("branch") String branch, @Param("latestCommit") String latestCommit);

    String getScanStatus(@Param("repoId") String repoId, @Param("branch") String branch);

    String getLatestScan(@Param("repoId") String repoId, @Param("branch") String branch);
}
