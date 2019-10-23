/**
 * @description:
 * @author: fancying
 * @create: 2019-09-29 17:01
 **/
package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectInfo.ClassInfo;
import cn.edu.fudan.codetracker.domain.projectInfo.TrackerInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassMapper {

    void insertClassInfoList(List<ClassInfo> classInfos);

    void insertRawClassInfoList(List<ClassInfo> classInfos);

    TrackerInfo getTrackerInfo(String filePath, String className);

    void setDeleteInfo(List<ClassInfo> classInfosList);

    void setChangeInfo(List<ClassInfo> classInfosList);
}