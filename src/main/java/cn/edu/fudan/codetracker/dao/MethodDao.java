/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:48
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectInfo.MethodInfo;
import cn.edu.fudan.codetracker.mapper.MethodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MethodDao {

    private MethodMapper methodMapper;

    public void insertMethodInfoList(List<MethodInfo> methodInfos) {
        methodMapper.insertMethodInfoList(methodInfos);
    }

    public void insertRawMethodInfoList(List<MethodInfo> methodInfos) {
        methodMapper.insertRawMethodInfoList(methodInfos);
    }

    @Autowired
    public void setMethodMapper(MethodMapper methodMapper) {
        this.methodMapper = methodMapper;
    }
}