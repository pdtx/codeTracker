/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:47
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.ClassInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import cn.edu.fudan.codetracker.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class ClassDao {

    @Autowired
    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    private ClassMapper classMapper;

    public void insertClassInfoList(List<ClassInfo> classInfos) {
        classMapper.insertClassInfoList(classInfos);
    }

    public void insertRawClassInfoList(List<ClassInfo> classInfos) {
        classMapper.insertRawClassInfoList(classInfos);
    }


    public TrackerInfo getTrackerInfo(String filePath, String className, String repoUuid, String branch) {
        return classMapper.getTrackerInfo(filePath, className, repoUuid, branch);
    }

    public void setAddInfo(Set<ClassInfo> classInfos) {
        if (classInfos.size() == 0) {
            return;
        }
        List<ClassInfo> classInfosList = new ArrayList<>(classInfos);
        insertClassInfoList(classInfosList);
        insertRawClassInfoList(classInfosList);
    }

    public void setDeleteInfo(Set<ClassInfo> classInfos) {
        if (classInfos.size() == 0) {
            return;
        }
        List<ClassInfo> classInfosList = new ArrayList<>(classInfos);
        //classMapper.setDeleteInfo(classInfosList);
        insertRawClassInfoList(classInfosList);
    }

    public void setChangeInfo(Set<ClassInfo> classInfos) {
        if (classInfos.size() == 0) {
            return;
        }
        List<ClassInfo> classInfosList = new ArrayList<>(classInfos);
        classMapper.updateChangeInfo(classInfosList);
        insertRawClassInfoList(classInfosList);
    }
}