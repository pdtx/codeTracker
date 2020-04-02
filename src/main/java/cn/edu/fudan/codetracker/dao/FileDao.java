/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:47
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FileInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FileNode;
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

    @Autowired
    public void setFileMapper(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    public void insertFileInfoList(List<FileNode> fileNodes, CommonInfo commonInfo) {
        fileMapper.insertFileInfoList(fileNodes, commonInfo);
    }

    public void insertRawFileInfoList(List<FileNode> fileNodes, CommonInfo commonInfo) {
        fileMapper.insertRawFileInfoList(fileNodes, commonInfo);
    }

    public void setAddInfo(Set<FileNode> fileNodes, CommonInfo commonInfo) {
        if (fileNodes.isEmpty()) {
            return;
        }
        List<FileNode> fileInfoList = new ArrayList<>(fileNodes);
        insertFileInfoList(fileInfoList, commonInfo);
        insertRawFileInfoList(fileInfoList, commonInfo);
    }

    public void setDeleteInfo(Set<FileNode> fileNodes, CommonInfo commonInfo) {
        if (fileNodes.isEmpty()) {
            return;
        }
        List<FileNode> fileInfoList = new ArrayList<>(fileNodes);
        insertRawFileInfoList(fileInfoList, commonInfo);
    }

    public void setChangeInfo(Set<FileNode> fileNodes, CommonInfo commonInfo) {
        if (fileNodes.isEmpty()) {
            return;
        }
        List<FileNode> fileInfoList = new ArrayList<>(fileNodes);
        fileMapper.updateChangeInfo(fileInfoList, commonInfo);
        insertRawFileInfoList(fileInfoList, commonInfo);
    }

    public TrackerInfo getTrackerInfo(String path, String repoUuid, String branch) {
        return fileMapper.getTrackerInfo(path, repoUuid, branch);
    }

}