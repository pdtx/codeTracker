
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.ClassNode;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 * @author fancying
 * create: 2019-09-26 18:47
 **/
@Repository
public class ClassDao {
    private ClassMapper classMapper;

    @Autowired
    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public void insertClassInfoList(List<ClassNode> classNodes, CommonInfo commonInfo) {
        classMapper.insertClassInfoList(classNodes, commonInfo);
    }

    public void insertRawClassInfoList(List<ClassNode> classNodes, CommonInfo commonInfo) {
        classMapper.insertRawClassInfoList(classNodes, commonInfo);
    }

    public void setAddInfo(Set<ClassNode> classNodes, CommonInfo commonInfo) {
        if (classNodes.size() == 0) {
            return;
        }
        List<ClassNode> classInfosList = new ArrayList<>(classNodes);
        insertClassInfoList(classInfosList, commonInfo);
        insertRawClassInfoList(classInfosList, commonInfo);
    }

    public void setDeleteInfo(Set<ClassNode> classNodes, CommonInfo commonInfo) {
        if (classNodes.size() == 0) {
            return;
        }
        List<ClassNode> classInfosList = new ArrayList<>(classNodes);
        insertRawClassInfoList(classInfosList, commonInfo);
    }

    public void setChangeInfo(Set<ClassNode> classNodes, CommonInfo commonInfo) {
        if (classNodes.size() == 0) {
            return;
        }
        List<ClassNode> classInfosList = new ArrayList<>(classNodes);
        classMapper.updateChangeInfo(classInfosList, commonInfo);
        insertRawClassInfoList(classInfosList, commonInfo);
    }

    public void updateMetaInfo(Set<ClassNode> classNodes, CommonInfo commonInfo) {
        if (classNodes.size() == 0) {
            return;
        }
        List<ClassNode> classInfosList = new ArrayList<>(classNodes);
        classMapper.updateChangeInfo(classInfosList, commonInfo);
    }


    public TrackerInfo getTrackerInfo(String filePath, String className, String repoUuid, String branch) {
        return classMapper.getTrackerInfo(filePath, className, repoUuid, branch);
    }
}