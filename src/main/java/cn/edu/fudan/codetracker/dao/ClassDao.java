/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:47
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectInfo.ClassInfo;
import cn.edu.fudan.codetracker.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClassDao {

    @Autowired
    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    private ClassMapper classMapper;

    public void insertClassInfoList(List<ClassInfo> classInfos) {
        classMapper.insertClassInfoList(classInfos);
    }

    public void insertRawClassInfoList(List<ClassInfo> classInfos) {
        classMapper.insertRawClassInfoList(classInfos);
    }


}