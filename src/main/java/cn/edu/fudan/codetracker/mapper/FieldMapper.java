package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.FieldInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.TrackerInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldMapper {


    void insertFieldInfoList(List<FieldInfo> fieldInfos);

    void insertRawFieldInfoList(List<FieldInfo> fieldInfos);

    TrackerInfo getTrackerInfo(String filePath, String className, String simpleName, String repoUuid, String branch);

    void updateChangeInfo(List<FieldInfo> fieldInfos);


}
