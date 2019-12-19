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
        List<DeveloperMostFocus> developerMostFocusList = new ArrayList<>();
        List<DeveloperMostFocus> temp;
        if (ProjectInfoLevel.METHOD.name().equals(type)) {
            temp = statisticsMapper.methodDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                List<String> content = statisticsMapper.getContentByMethodId(dmf.getUuid(),committer,beginDate,endDate);
                dmf.setContent(content);
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
        }

        if (ProjectInfoLevel.CLASS.name().equals(type)) {
            temp = statisticsMapper.classDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
        }

        if (ProjectInfoLevel.FILE.name().equals(type)) {
            temp = statisticsMapper.fileDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
        }

        if (ProjectInfoLevel.PACKAGE.name().equals(type)) {
            temp = statisticsMapper.packageDeveloperFocusMost(committer, beginDate, endDate);
            for (DeveloperMostFocus dmf: temp) {
                developerMostFocusList.add(dmf);
            }
            return developerMostFocusList;
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


    /**
     * get delete statement former info by committer in given time
     */
    public List<DeleteStatementInfo> getDeleteStatementFormerInfoByCommitter(String committer, String repoUuid, String branch, String beginDate, String endDate){
        List<DeleteStatementInfo> statementUuidList = statisticsMapper.getDeleteStatementUuidList(committer, repoUuid, branch, beginDate, endDate);
        List<DeleteStatementInfo> deleteStatementInfoList = new ArrayList<>();
        for (DeleteStatementInfo deleteStatementInfo : statementUuidList) {
            DeleteStatementInfo deleteStatementInfo1 = statisticsMapper.getDeleteStatementFirstInfo(deleteStatementInfo.getStatementUuid());
            DeleteStatementInfo deleteStatementInfo2 = statisticsMapper.getDeleteStatementLastInfo(deleteStatementInfo.getStatementUuid());
            deleteStatementInfo.setFirstCommitter(deleteStatementInfo1.getFirstCommitter());
            deleteStatementInfo.setFirstCommitDate(deleteStatementInfo1.getFirstCommitDate());
            deleteStatementInfo.setLastCommitter(deleteStatementInfo2.getLastCommitter());
            deleteStatementInfo.setLastCommitDate(deleteStatementInfo2.getLastCommitDate());
            deleteStatementInfo.setBody(deleteStatementInfo2.getBody());
            deleteStatementInfoList.add(deleteStatementInfo);
        }
        return deleteStatementInfoList;
    }


    /**
     * get statement info by method and committer in given time
     */
    public List<StatementInfoByMethod> getStatementInfoByMethod(String committer, String methodUuid, String beginDate, String endDate){
        return statisticsMapper.getStatementInfoByMethod(committer, methodUuid, beginDate, endDate);
    }


    /**
     * get change committer
     */
    public String getChangeCommitter(String type, String... args) {
        switch (type) {
            case "class":
                return statisticsMapper.getChangeCommitterByClass(args[0], args[1], args[2], args[3], args[4]);
            case "method":
                return statisticsMapper.getChangeCommitterByMethod(args[0], args[1], args[2], args[3], args[4], args[5]);
            case "field":
                return statisticsMapper.getChangeCommitterByField(args[0], args[1], args[2], args[3], args[4], args[5]);
            case "statement":
                return statisticsMapper.getChangeCommitterByStatement(args[0], args[1], args[2]);
            default:
                return "";
        }
    }

    /**
     * get committer line info by commit
     */
    public List<CommitterLineInfo> getCommitterLineInfo(String repoUuid, String branch, String commitDate) {
        return statisticsMapper.getCommitterLineInfo(repoUuid, branch, commitDate);
    }


    public String getMetaMethodUuidByMethod(String filePath, String repoUuid, String branch, String className, String signature, String commitDate) {
        return statisticsMapper.getMetaMethodUuidByMethod(filePath, repoUuid, branch, className, signature, commitDate);
    }


    /**
     * 临时接口
     */
    public List<TempMostInfo> getFocus(String committer) {
        List<MostModifiedInfo> packageInfos = statisticsMapper.getPackageInfoMost(committer);
        List<TempMostInfo> packageList = new ArrayList<>();
        for (MostModifiedInfo mostModifiedInfo: packageInfos) {
            TempMostInfo packageInfo = new TempMostInfo();
            packageInfo.setName(mostModifiedInfo.getPackageName());
            packageInfo.setQuantity(mostModifiedInfo.getVersion());
            packageInfo.setUuid(mostModifiedInfo.getUuid());
            List<MostModifiedInfo> classInfos = statisticsMapper.getClassInfoMost(committer,mostModifiedInfo.getModuleName(),mostModifiedInfo.getPackageName());
            List<TempMostInfo> classList = new ArrayList<>();
            for (MostModifiedInfo modifiedInfo : classInfos) {
                TempMostInfo classInfo = new TempMostInfo();
                classInfo.setName(modifiedInfo.getClassName());
                classInfo.setQuantity(modifiedInfo.getVersion());
                classInfo.setUuid(modifiedInfo.getUuid());
                List<MostModifiedInfo> methodInfos = statisticsMapper.getMethodInfoMost(committer,modifiedInfo.getFilePath(),modifiedInfo.getClassName());
                List<TempMostInfo> methodList = new ArrayList<>();
                for (MostModifiedInfo methodInfo : methodInfos) {
                    TempMostInfo method = new TempMostInfo();
                    method.setName(methodInfo.getMethodName());
                    method.setQuantity(methodInfo.getVersion());
                    method.setChildInfos(null);
                    method.setUuid(methodInfo.getUuid());
                    if (method.getQuantity()>1) {
                        methodList.add(method);
                    }
                }
                classInfo.setChildInfos(methodList);
                if (classInfo.getQuantity()>1) {
                    classList.add(classInfo);
                }
            }
            packageInfo.setChildInfos(classList);
            if (packageInfo.getQuantity()>1) {
                packageList.add(packageInfo);
            }
        }
        return packageList;
    }


    /**
     * 临时接口
     */
    public List<MethodHistory> getMethodHistory(String methodUuid) {
        return statisticsMapper.getMethodHistory(methodUuid);
    }


}