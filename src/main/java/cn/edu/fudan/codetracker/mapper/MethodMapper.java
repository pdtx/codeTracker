package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.MethodNode;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodMapper {

    void insertMethodInfoList(@Param("methodNodes") List<MethodNode> methodNodes, @Param("commonInfo")CommonInfo commonInfo);

    void insertRawMethodInfoList(@Param("methodNodes") List<MethodNode> methodNodes, @Param("commonInfo")CommonInfo commonInfo);

    void updateChangeInfo(@Param("methodNodes") List<MethodNode> methodNodes, @Param("commonInfo")CommonInfo commonInfo);



    TrackerInfo getTrackerInfo(@Param("filePath") String filePath, @Param("className") String className, @Param("signature") String signature, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("content") String content);

}
