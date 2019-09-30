package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectInfo.MethodInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodMapper {

    void insertMethodInfoList(List<MethodInfo> methodInfos);

    void insertRawMethodInfoList(List<MethodInfo> methodInfos);
}
