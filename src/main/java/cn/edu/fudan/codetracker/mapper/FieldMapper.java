package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectInfo.FieldInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldMapper {


    void insertFieldInfoList(List<FieldInfo> fieldInfos);

    void insertRawFieldInfoList(List<FieldInfo> fieldInfos);
}
