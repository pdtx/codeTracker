package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectInfo.FieldInfo;
import cn.edu.fudan.codetracker.domain.projectInfo.TrackerInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface FieldMapper {


    void insertFieldInfoList(List<FieldInfo> fieldInfos);

    void insertRawFieldInfoList(List<FieldInfo> fieldInfos);

    TrackerInfo getTrackerInfo(String filePath, String className, String simpleName);

    void updateChangeInfo(List<FieldInfo> fieldInfos);


}
