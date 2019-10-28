/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:47
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.FileInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public TrackerInfo getTrackerInfo(String path) {
        return fileMapper.getTrackerInfo(path);
    }

    public void setAddInfo(Set<FileInfo> fileInfos) {
        if (fileInfos.isEmpty())
            return;
        List<FileInfo> fileInfoList = new ArrayList<>(fileInfos);
        insertFileInfoList(fileInfoList);
        insertRawFileInfoList(fileInfoList);
    }

    public void setDeleteInfo(Set<FileInfo> fileInfos) {
        if (fileInfos.isEmpty())
            return;
        List<FileInfo> fileInfoList = new ArrayList<>(fileInfos);
        //fileMapper.setDeleteInfo(fileInfoList);
        insertRawFileInfoList(fileInfoList);
    }

    public void setChangeInfo(Set<FileInfo> fileInfos) {
        if (fileInfos.isEmpty())
            return;
        List<FileInfo> fileInfoList = new ArrayList<>(fileInfos);
        fileMapper.updateChangeInfo(fileInfoList);
        insertRawFileInfoList(fileInfoList);
    }
}