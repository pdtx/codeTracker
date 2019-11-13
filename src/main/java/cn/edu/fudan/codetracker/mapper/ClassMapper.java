/**
 * @description:
 * @author: fancying
 * @create: 2019-09-29 17:01
 **/
package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.ClassInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassMapper {

    void insertClassInfoList(List<ClassInfo> classInfos);

    void insertRawClassInfoList(List<ClassInfo> classInfos);

    TrackerInfo getTrackerInfo(@Param("filePath") String filePath, @Param("className") String className, @Param("repoUuid") String repoUuid, @Param("branch") String branch);

    void updateChangeInfo(List<ClassInfo> classInfosList);
}