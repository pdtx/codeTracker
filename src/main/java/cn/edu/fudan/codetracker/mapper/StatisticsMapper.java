package cn.edu.fudan.codetracker.mapper;

/**
 * @author: fancying
 * @create: 2019-06-06 16:41
 */
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsMapper {
    /**
     * distribution of method modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getMethodStatistics(String repoUuid, String branch);
    /**
     * distribution of class modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getClassStatistics(String repoUuid, String branch);
    /**
     * distribution of file modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getFileStatistics(String repoUuid, String branch);



    /**
     * most modified method
     */
    List<MostModifiedInfo> getMostModifiedMethod(String repoUuid, String branch);
    /**
     * most modified file
     */
    List<MostModifiedInfo> getMostModifiedFile(String repoUuid, String branch);
    /**
     * most modified class
     */
    List<MostModifiedInfo> getMostModifiedClass(String repoUuid, String branch);

    /**
     * file modification of most developers participate in
     */
    List<VersionStatistics> getModifiedFileStatistics(String repoUuid, String branch);
    /**
     * class modification of most developers participate in
     */
    List<VersionStatistics> getModifiedClassStatistics(String repoUuid, String branch);
    /**
     * method modification of most developers participate in
     */
    List<VersionStatistics> getModifiedMethodStatistics(String repoUuid, String branch);
}
