package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.FieldInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldMapper {


    void insertFieldInfoList(List<FieldInfo> fieldInfos);

    void insertRawFieldInfoList(List<FieldInfo> fieldInfos);

    TrackerInfo getTrackerInfo(@Param("filePath") String filePath, @Param("className") String className, @Param("simpleName") String simpleName, @Param("repoUuid") String repoUuid, @Param("branch") String branch);

    void updateChangeInfo(List<FieldInfo> fieldInfos);


}
