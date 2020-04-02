/**
 * @description:
 * @author: fancying
 * @create: 2019-09-29 17:01
 **/
package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.ClassInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.ClassNode;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassMapper {

    void insertClassInfoList(@Param("classNodes") List<ClassNode> classNodes, @Param("commonInfo")CommonInfo commonInfo);

    void insertRawClassInfoList(@Param("classNodes") List<ClassNode> classNodes, @Param("commonInfo")CommonInfo commonInfo);

    void updateChangeInfo(@Param("classNodes") List<ClassNode> classNodes, @Param("commonInfo")CommonInfo commonInfo);



    TrackerInfo getTrackerInfo(@Param("filePath") String filePath, @Param("className") String className, @Param("repoUuid") String repoUuid, @Param("branch") String branch);
}