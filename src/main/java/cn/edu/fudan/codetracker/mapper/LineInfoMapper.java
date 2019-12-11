package cn.edu.fudan.codetracker.mapper;

import cn.edu.fudan.codetracker.domain.LineInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface LineInfoMapper {
    void insertLineInfo(LineInfo lineInfo);
}
