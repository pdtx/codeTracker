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

    TrackerInfo getTrackerInfo(String moduleName, String packageName, String fileName, String className, String simpleName);

    void setChangeInfo(List<FieldInfo> fieldInfos);

    void setDeleteInfo(List<FieldInfo> fieldInfos);

}
