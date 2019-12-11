package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.LineInfo;
import cn.edu.fudan.codetracker.mapper.LineInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LineInfoDao {
    private LineInfoMapper lineInfoMapper;

    @Autowired
    public void setLineInfoMapper(LineInfoMapper lineInfoMapper) { this.lineInfoMapper = lineInfoMapper; }

    public void insertLineInfo(LineInfo lineInfo){
        lineInfoMapper.insertLineInfo(lineInfo);
    }
}
