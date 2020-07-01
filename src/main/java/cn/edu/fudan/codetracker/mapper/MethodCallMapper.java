package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.MethodCall;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MethodCallMapper {

    void insertMethodCallList(@Param("methodCalls") List<MethodCall> methodCalls, @Param("commonInfo") CommonInfo commonInfo);

}
