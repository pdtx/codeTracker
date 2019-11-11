package cn.edu.fudan.codetracker.mapper;

/**
 * @author: fancying
 * @create: 2019-06-06 16:41
 */
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsMapper {
    /**
     * get
     *
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getMethodStatistics(String repoUuid, String branch);

    List getMethod(int version, String repoUuid, String branch);

}
