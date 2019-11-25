/**
 * @description:
 * @author: fancying
 * @create: 2019-11-12 09:59
 **/
package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMethodStatistics(repoUuid, branch);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getClassStatistics(repoUuid, branch);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getFileStatistics(repoUuid, branch);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.getPackageStatistics(repoUuid, branch);
        }
        return null;
    }

    /**
     * most modified
     */
    public List<MostModifiedInfo> getMostModifiedInfo(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMostModifiedMethod(repoUuid, branch);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getMostModifiedClass(repoUuid, branch);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getMostModifiedFile(repoUuid, branch);
        }
        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostModifiedPackage(repoUuid,branch);
        }
        return null;
    }

    /**
     * modification of most developers participate in
     */
    public List<MostDevelopersInfo> getMostDevelopersInvolved(String repoUuid, String branch, String type) {
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedMethod(repoUuid, branch);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedClass(repoUuid, branch);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedFile(repoUuid, branch);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.getMostDevelopersInvolvedPackage(repoUuid, branch);
        }
        return null;
    }

    /**
     * most modified in given time
     */
    public List<MostDevelopersInfo> getMostModifiedByTime(String repoUuid, String branch, String type,String beginDate, String endDate){
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.getMostModifiedMethodByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.getMostModifiedClassByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.getMostModifiedFileByTime(repoUuid,branch, beginDate, endDate);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
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
    public List<DeveloperMostFocus> getDeveloperFocusMost(String type, String committer, String beginDate, String endDate){
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.methodDeveloperFocusMost(committer, beginDate, endDate);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.classDeveloperFocusMost(committer, beginDate, endDate);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.fileDeveloperFocusMost(committer, beginDate, endDate);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.packageDeveloperFocusMost(committer, beginDate, endDate);
        }
        return null;
    }

    /**
     * get commit info by uuid
     */
    public List<CommitTimeLine> getCommitTimeLine(String type, String uuid){
        type = type.toUpperCase();
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            return statisticsMapper.methodCommitTimeLine(uuid);
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            return statisticsMapper.classCommitTimeLine(uuid);
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            return statisticsMapper.fileCommitTimeLine(uuid);
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            return statisticsMapper.packageCommitTimeLine(uuid);
        }
        return null;
    }


    /**
     * get commit info by committer
     */
    public List<CommitterHistory> getCommitInfoByCommitter(String committer){
        List<CommitInfoByCommitter> commitList = statisticsMapper.getCommitInfoByCommitter(committer);
        List<CommitterHistory> historyList = new ArrayList<>();
        for (CommitInfoByCommitter cInfo: commitList) {
            List<BasicInfoByCommitId> fileInfo = new ArrayList<>();
            List<BasicInfoByCommitId> methodInfo = new ArrayList<>();
            fileInfo = statisticsMapper.getFileInfoByCommitId(cInfo.getCommitId());
            methodInfo = statisticsMapper.getMethodInfoByCommitId(cInfo.getCommitId());
            CommitterHistory cHistory = new CommitterHistory();
            cHistory.setCommitId(cInfo.getCommitId());
            cHistory.setCommitDate(cInfo.getCommitDate());
            cHistory.setCommitMessage(cInfo.getCommitMessage());
            cHistory.setFileList(fileInfo);
            cHistory.setMethodList(methodInfo);
            historyList.add(cHistory);
        }
        return historyList;
    }




}