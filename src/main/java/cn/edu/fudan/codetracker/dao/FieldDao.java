
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.ClassNode;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FieldNode;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.FieldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 * description:
 * @author fancying
 * create: 2019-09-26 18:48
 **/
@Repository
public class FieldDao {

    private FieldMapper fieldMapper;

    @Autowired
    public void setFieldMapper(FieldMapper fieldMapper) {
        this.fieldMapper = fieldMapper;
    }

    public void insertFieldInfoList(List<FieldNode> fieldNodes, CommonInfo commonInfo) {
        fieldMapper.insertFieldInfoList(fieldNodes, commonInfo);
    }

    public void insertRawFieldInfoList(List<FieldNode> fieldNodes, CommonInfo commonInfo) {
        fieldMapper.insertRawFieldInfoList(fieldNodes, commonInfo);
    }

    public void setAddInfo(Set<FieldNode> fieldNodes, CommonInfo commonInfo) {
        if (fieldNodes.size() == 0) {
            return;
        }
        List<FieldNode> fieldInfoList = new ArrayList<>(fieldNodes);
        insertFieldInfoList(fieldInfoList, commonInfo);
        insertRawFieldInfoList(fieldInfoList, commonInfo);
    }

    public void setDeleteInfo(Set<FieldNode> fieldNodes, CommonInfo commonInfo) {
        if (fieldNodes.size() == 0) {
            return;
        }
        List<FieldNode> fieldInfoList = new ArrayList<>(fieldNodes);
        insertRawFieldInfoList(fieldInfoList, commonInfo);
    }

    public void setChangeInfo(Set<FieldNode> fieldNodes, CommonInfo commonInfo) {
        if (fieldNodes.size() == 0) {
            return;
        }
        List<FieldNode> fieldInfoList = new ArrayList<>(fieldNodes);
        fieldMapper.updateChangeInfo(fieldInfoList, commonInfo);
        insertRawFieldInfoList(fieldInfoList, commonInfo);
    }

    public void updateMetaInfo(Set<FieldNode> fieldNodes, CommonInfo commonInfo) {
        if (fieldNodes.size() == 0) {
            return;
        }
        List<FieldNode> fieldInfoList = new ArrayList<>(fieldNodes);
        fieldMapper.updateChangeInfo(fieldInfoList, commonInfo);
    }


    public TrackerInfo getTrackerInfo(String filePath, String className, String simpleName, String repoUuid, String branch) {
        return fieldMapper.getTrackerInfo( filePath, className, simpleName, repoUuid, branch);
    }
}