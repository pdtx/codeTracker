package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FileNode;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMapper {
    /**
     * track_file
     * */
    void insertFileInfoList(@Param("fileNodes") List<FileNode> fileNodes, @Param("commonInfo")CommonInfo commonInfo);

    /**
     * raw_file
     * */
    void insertRawFileInfoList(@Param("fileNodes") List<FileNode> fileNodes, @Param("commonInfo")CommonInfo commonInfo);

    void updateChangeInfo(@Param("fileNodes") List<FileNode> fileNodes, @Param("commonInfo")CommonInfo commonInfo);



    TrackerInfo getTrackerInfo(@Param("path") String path, @Param("repoUuid") String repoUuid, @Param("branch") String branch);
}
