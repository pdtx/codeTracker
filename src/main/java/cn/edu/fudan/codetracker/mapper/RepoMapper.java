package cn.edu.fudan.codetracker.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoMapper {

    void insertScanRepo(@Param("uuid") String uuid, @Param("repoId") String repoId, @Param("branch") String branch, @Param("status") String status);

    void updateScanStatus(@Param("repoId") String repoId, @Param("branch") String branch, @Param("status") String status);

    void updateLatestCommit(@Param("repoId") String repoId, @Param("branch") String branch, @Param("latestCommit") String latestCommit);

    String getScanStatus(@Param("repoId") String repoId, @Param("branch") String branch);

    @Select("SELECT latest_commit FROM tracker_repo WHERE repo_id = #{repoId} AND branch = #{branch};")
    String getLatestScan(@Param("repoId") String repoId, @Param("branch") String branch);
}
