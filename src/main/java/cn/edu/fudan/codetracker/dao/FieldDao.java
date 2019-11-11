/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:48
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.FieldInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.FieldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class FieldDao {

    private FieldMapper fieldMapper;

    public void insertFieldInfoList(List<FieldInfo> fieldInfos) {
        fieldMapper.insertFieldInfoList(fieldInfos);
    }

    public void insertRawFieldInfoList(List<FieldInfo> fieldInfos) {
        fieldMapper.insertRawFieldInfoList(fieldInfos);
    }

    @Autowired
    public void setFieldMapper(FieldMapper fieldMapper) {
        this.fieldMapper = fieldMapper;
    }

    public TrackerInfo getTrackerInfo(String filePath, String className, String simpleName, String repoUuid, String branch) {
        return fieldMapper.getTrackerInfo( filePath, className, simpleName, repoUuid, branch);
    }

    public void setAddInfo(Set<FieldInfo> fieldInfos) {
        if (fieldInfos.size() == 0) {
            return;
        }
        List<FieldInfo> fieldInfoList = new ArrayList<>(fieldInfos);
        insertFieldInfoList(fieldInfoList);
        insertRawFieldInfoList(fieldInfoList);
    }

    public void setDeleteInfo(Set<FieldInfo> fieldInfos) {
        if (fieldInfos.size() == 0) {
            return;
        }
        List<FieldInfo> fieldInfoList = new ArrayList<>(fieldInfos);
        //fieldMapper.setDeleteInfo(fieldInfoList);
        insertRawFieldInfoList(fieldInfoList);
    }

    public void setChangeInfo(Set<FieldInfo> fieldInfos) {
        if (fieldInfos.size() == 0) {
            return;
        }
        List<FieldInfo> fieldInfoList = new ArrayList<>(fieldInfos);
        fieldMapper.updateChangeInfo(fieldInfoList);
        insertRawFieldInfoList(fieldInfoList);
    }
}