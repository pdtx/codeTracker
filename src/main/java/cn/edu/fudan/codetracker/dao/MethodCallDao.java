package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.MethodCall;
import cn.edu.fudan.codetracker.mapper.MethodCallMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author chenyuan
 */
@Repository
public class MethodCallDao {

    private MethodCallMapper methodCallMapper;

    @Autowired
    public void setMethodCallMapper(MethodCallMapper methodCallMapper) {
        this.methodCallMapper = methodCallMapper;
    }

    public void insertMethodCallList(List<MethodCall> methodCalls, CommonInfo commonInfo) {
        if (methodCalls == null || methodCalls.size() == 0) {
            return;
        }
        methodCallMapper.insertMethodCallList(methodCalls, commonInfo);
    }
}
