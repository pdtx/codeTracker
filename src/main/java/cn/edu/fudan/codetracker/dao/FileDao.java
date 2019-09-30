/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:47
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectInfo.FileInfo;
import cn.edu.fudan.codetracker.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FileDao {

    private FileMapper fileMapper;

    public void insertFileInfoList(List<FileInfo> fileInfos) {
        fileMapper.insertFileInfoList(fileInfos);
    }

    public void insertRawFileInfoList(List<FileInfo> fileInfos) {
        fileMapper.insertRawFileInfoList(fileInfos);
    }
    @Autowired
    public void setFileMapper(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }
}