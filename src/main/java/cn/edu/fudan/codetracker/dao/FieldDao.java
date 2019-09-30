/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 18:48
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.projectInfo.FieldInfo;
import cn.edu.fudan.codetracker.mapper.FieldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FieldDao {

    private FieldMapper fieldMapper;

    public void insertFieldInfoList(List<FieldInfo> fieldInfos) {
        fieldMapper.insertFieldInfoList(fieldInfos);
    }

    public void insertRawFieldInfoList(List<FieldInfo> fieldInfos) {
        fieldMapper.insertRawFieldInfoList(fieldInfos);
    }

    @Autowired
    public void setFieldMapper(FieldMapper fieldMapper) {
        this.fieldMapper = fieldMapper;
    }
}