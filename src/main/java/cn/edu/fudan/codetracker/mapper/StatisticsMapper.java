package cn.edu.fudan.codetracker.mapper;

/**
 * @author: fancying
 * @create: 2019-06-06 16:41
 */
import cn.edu.fudan.codetracker.domain.resultmap.MostDevelopersInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedMethod;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
import org.apache.ibatis.annotations.Param;
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
    List<VersionStatistics> getMethodStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * distribution of class modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getClassStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * distribution of file modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getFileStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * distribution of package modification times
     * @param repoUuid repo uuid
     * @param branch branch
     * @return list
     */
    List<VersionStatistics> getPackageStatistics(@Param("repoUuid") String repoUuid, @Param("branch") String branch);



    /**
     * most modified method
     */
    List<MostModifiedInfo> getMostModifiedMethod(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * most modified file
     */
    List<MostModifiedInfo> getMostModifiedFile(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * most modified class
     */
    List<MostModifiedInfo> getMostModifiedClass(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * most modified package
     */
    List<MostModifiedInfo> getMostModifiedPackage(@Param("repoUuid") String repoUuid, @Param("branch") String branch);


    /**
     * file modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedFile(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * class modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedClass(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * method modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedMethod(@Param("repoUuid") String repoUuid, @Param("branch") String branch);
    /**
     * package modification of most developers participate in
     */
    List<MostDevelopersInfo> getMostDevelopersInvolvedPackage(@Param("repoUuid") String repoUuid, @Param("branch") String branch);



    /**
     * most modified package in given time
     */
    List<MostDevelopersInfo> getMostModifiedPackageByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * most modified file in given time
     */
    List<MostDevelopersInfo> getMostModifiedFileByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * most modified class in given time
     */
    List<MostDevelopersInfo> getMostModifiedClassByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);
    /**
     * most modified method in given time
     */
    List<MostDevelopersInfo> getMostModifiedMethodByTime(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate);


    /**
     * get most modified methods info in given package
     */
    List<MostModifiedMethod> getMostModifiedMethodByPackage(@Param("repoUuid") String repoUuid, @Param("packageUuid") String packageUuid, @Param("branch") String branch);
}
