package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.FileInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMapper {


    /**
     * raw_file
     * */
    void insertRawFileInfoList(List<FileInfo> fileInfos);

    /**
     * track_file
     * */
    void insertFileInfoList(List<FileInfo> fileInfos);

    TrackerInfo getTrackerInfo(@Param("path") String path, @Param("repoUuid") String repoUuid, @Param("branch") String branch);

    //void setDeleteInfo(List<FileInfo> fileInfoList);

    void updateChangeInfo(List<FileInfo> fileInfoList);
}
