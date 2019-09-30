package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectInfo.FileInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMapper {

    void insertFileInfoList(List<FileInfo> fileInfos);

    void insertRawFileInfoList(List<FileInfo> fileInfos);
}
