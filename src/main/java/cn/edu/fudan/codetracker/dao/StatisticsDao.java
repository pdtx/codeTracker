/**
 * @description:
 * @author: fancying
 * @create: 2019-11-12 09:59
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.ProjectInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostDevelopersInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedMethod;
import cn.edu.fudan.codetracker.domain.resultmap.VersionStatistics;
import cn.edu.fudan.codetracker.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StatisticsDao {
    private StatisticsMapper statisticsMapper;

    @Autowired
    public void setStatisticsMapper(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    public List<VersionStatistics> getStatisticsByType(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfo.METHOD.name().equals(type)) {
            return statisticsMapper.getMethodStatistics(repoUuid, branch);
        }

        if (ProjectInfo.CLASS.name().equals(type)) {
            return statisticsMapper.getClassStatistics(repoUuid, branch);
        }

        if (ProjectInfo.FILE.name().equals(type)) {
            return statisticsMapper.getFileStatistics(repoUuid, branch);
        }

        if (ProjectInfo.PACKAGE.name().equals(type)) {
            return statisticsMapper.getPackageStatistics(repoUuid, branch);
        }
        return null;
    }

    /**
     * most modified
     */
    public List<MostModifiedInfo> getMostModifiedInfo(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfo.METHOD.name().equals(type)) {
            return statisticsMapper.getMostModifiedMethod(repoUuid, branch);
        }

        if (ProjectInfo.CLASS.name().equals(type)) {
            return statisticsMapper.getMostModifiedClass(repoUuid, branch);
        }

        if (ProjectInfo.FILE.name().equals(type)) {
            return statisticsMapper.getMostModifiedFile(repoUuid, branch);
        }
        if (ProjectInfo.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostModifiedPackage(repoUuid,branch);
        }
        return null;
    }

    /**
     * modification of most developers participate in
     */
    public List<MostDevelopersInfo> getMostDevelopersInvolved(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfo.METHOD.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedMethod(repoUuid, branch);
        }

        if (ProjectInfo.CLASS.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedClass(repoUuid, branch);
        }

        if (ProjectInfo.FILE.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedFile(repoUuid, branch);
        }

        if (ProjectInfo.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedPackage(repoUuid, branch);
        }
        return null;
    }

    /**
     * most modified in given time
     */
    public List<MostDevelopersInfo> getMostModifiedByTime(String repoUuid, String branch, String type,String beginDate, String endDate){
        type = type.toUpperCase();
        if (ProjectInfo.METHOD.name().equals(type)) {
            return statisticsMapper.getMostModifiedMethodByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfo.CLASS.name().equals(type)) {
            return statisticsMapper.getMostModifiedClassByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfo.FILE.name().equals(type)) {
            return statisticsMapper.getMostModifiedFileByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfo.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostModifiedPackageByTime(repoUuid,branch, beginDate, endDate);
        }
        return null;
    }

    /**
     * get most modified methods info in given package
     */
    public List<MostModifiedMethod> getMostModifiedMethodByPackage(String repoUuid, String packageUuid, String branch){
        return statisticsMapper.getMostModifiedMethodByPackage(repoUuid, packageUuid, branch);
    }

    /**
     * developer most focus on in given time
     */
    public List<MostDevelopersInfo> getDeveloperFocusMost(String repoUuid, String type, String branch, String committer,String beginDate, String endDate){
        type = type.toUpperCase();
        if (ProjectInfo.METHOD.name().equals(type)) {
            return statisticsMapper.methodDeveloperFocusMost(repoUuid, branch, committer, beginDate, endDate);
        }

        if (ProjectInfo.CLASS.name().equals(type)) {
            return statisticsMapper.classDeveloperFocusMost(repoUuid, branch, committer, beginDate, endDate);
        }

        if (ProjectInfo.FILE.name().equals(type)) {
            return statisticsMapper.fileDeveloperFocusMost(repoUuid, branch, committer, beginDate, endDate);
        }

        if (ProjectInfo.PACKAGE.name().equals(type)) {
            return statisticsMapper.packageDeveloperFocusMost(repoUuid, branch, committer, beginDate, endDate);
        }
        return null;
    }


}