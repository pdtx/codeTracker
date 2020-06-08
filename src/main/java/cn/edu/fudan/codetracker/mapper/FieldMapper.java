package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FieldNode;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldMapper {


    void insertFieldInfoList(@Param("fieldNodes") List<FieldNode> fieldNodes, @Param("commonInfo")CommonInfo commonInfo);

    void insertRawFieldInfoList(@Param("fieldNodes") List<FieldNode> fieldNodes, @Param("commonInfo")CommonInfo commonInfo);

    void updateChangeInfo(@Param("fieldNodes") List<FieldNode> fieldNodes, @Param("commonInfo")CommonInfo commonInfo);



    TrackerInfo getTrackerInfo(@Param("filePath") String filePath, @Param("className") String className, @Param("simpleName") String simpleName, @Param("repoUuid") String repoUuid, @Param("branch") String branch);

}
