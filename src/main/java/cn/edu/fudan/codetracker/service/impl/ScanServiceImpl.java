/**
 * @description:
 * @author: fancying
 * @create: 2019-09-25 20:41
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.RelationShip;
import cn.edu.fudan.codetracker.handler.AnalyzeDiffFile;
import cn.edu.fudan.codetracker.handler.OutputAnalysis;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.ScanService;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import cn.edu.fudan.codetracker.util.cldiff.ClDiffHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ScanServiceImpl implements ScanService {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private PackageDao packageDao;
    private FileDao fileDao;
    private ClassDao classDao;
    private FieldDao fieldDao;
    private MethodDao methodDao;
    private StatementDao statementDao;

    private RestInterfaceManager restInterface;

    @Value("${outputDir}")
    private String outputDir;

    /**
     * 第一次扫描 存储项目结构
     * */
    @Override
    public void firstScan(String repoUuid, String branch, String duration) {
        String repoPath = IS_WINDOWS ? getRepoPathByUuid(repoUuid) : restInterface.getRepoPath(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndDuration(branch, duration);
        log.info("commit size : " +  commitList.size());
        RepoInfoBuilder repoInfo;
        boolean isInit = false;
        int num = 0;
        for (String commit : commitList) {
            ++num;
            if (isInit) {
                scan(repoUuid , commit, branch, jGitHelper, repoPath);
            } else {
                jGitHelper.checkout(commit);
                repoInfo = new RepoInfoBuilder(repoUuid, commit, repoPath, jGitHelper, branch, null);
                repoInfo.setCommitter(jGitHelper.getAuthorName(commit));
                saveData(repoInfo);
                isInit = true;
            }
            log.info("complete commit：" + num  + "  " + commit);
        }
    }

    private void scan (String repoUuid, String commitId, String branch, JGitHelper jGitHelper, String repoPath) {
        if (jGitHelper != null) {
            jGitHelper = new JGitHelper(repoPath);
        }

        // 分析版本之间的关系
        ClDiffHelper.executeDiff(repoPath, commitId, outputDir);
        String [] path = repoPath.replace('\\','/').split("/");
        String outputPath = outputDir +  (IS_WINDOWS ?  "\\" : "/") + path[path.length -1];
        // extra diff info and construct tracking relation
        OutputAnalysis analysis = new OutputAnalysis(repoUuid, branch, outputPath, jGitHelper, commitId);
        List<AnalyzeDiffFile> analyzeDiffFiles = analysis.analyzeMetaInfo(new ProxyDao(packageDao, fileDao, classDao, fieldDao, methodDao, statementDao));
        // 扫描结果记录入库
        try {

            for (AnalyzeDiffFile analyzeDiffFile : analyzeDiffFiles) {

                //add
                packageDao.setAddInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.ADD.name()));
                fileDao.setAddInfo(analyzeDiffFile.getFileInfos().get(RelationShip.ADD.name()));
                classDao.setAddInfo(analyzeDiffFile.getClassInfos().get(RelationShip.ADD.name()));
                methodDao.setAddInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.ADD.name()));
                fieldDao.setAddInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.ADD.name()));
                statementDao.setAddInfo(analyzeDiffFile.getStatementInfos().get(RelationShip.ADD.name()));
                //delete
                packageDao.setDeleteInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.DELETE.name()));
                fileDao.setDeleteInfo(analyzeDiffFile.getFileInfos().get(RelationShip.DELETE.name()));
                classDao.setDeleteInfo(analyzeDiffFile.getClassInfos().get(RelationShip.DELETE.name()));
                methodDao.setDeleteInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.DELETE.name()));
                fieldDao.setDeleteInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.DELETE.name()));
                statementDao.setDeleteInfo(analyzeDiffFile.getStatementInfos().get(RelationShip.DELETE.name()));
                //change
                packageDao.setChangeInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.CHANGE.name()));
                fileDao.setChangeInfo(analyzeDiffFile.getFileInfos().get(RelationShip.CHANGE.name()));
                classDao.setChangeInfo(analyzeDiffFile.getClassInfos().get(RelationShip.CHANGE.name()));
                methodDao.setChangeInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.CHANGE.name()));
                fieldDao.setChangeInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.CHANGE.name()));
                statementDao.setChangeInfo(analyzeDiffFile.getStatementInfos().get(RelationShip.CHANGE.name()));

            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveData(RepoInfoBuilder repoInfo) {
        try {

            packageDao.insertPackageInfoList(repoInfo.getPackageInfos());
            packageDao.insertRawPackageInfoList(repoInfo.getPackageInfos());

            fileDao.insertFileInfoList(repoInfo.getFileInfos());
            fileDao.insertRawFileInfoList(repoInfo.getFileInfos());

            classDao.insertClassInfoList(repoInfo.getClassInfos());
            classDao.insertRawClassInfoList(repoInfo.getClassInfos());

            methodDao.insertMethodInfoList(repoInfo.getMethodInfos());
            methodDao.insertRawMethodInfoList(repoInfo.getMethodInfos());

            fieldDao.insertFieldInfoList(repoInfo.getFieldInfos());
            fieldDao.insertRawFieldInfoList(repoInfo.getFieldInfos());

            statementDao.insertStatementInfoList(repoInfo.getStatementInfos());
            statementDao.insertRawStatementInfoList(repoInfo.getStatementInfos());
            statementDao.insertStatementRelationList(repoInfo.getStatementInfos());

        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    private String getRepoPathByUuid(String repoUuid) {
        if ("dubbo".equals(repoUuid)) {
            return "E:\\Lab\\project\\dubbo";
        }

        if("iec-wepm-develop".equals(repoUuid)) {
            return "E:\\Lab\\project\\iec-wepm-develop";
        }

        return "E:\\Lab\\project\\IssueTracker-Master-pre";
    }

    /**
     * getter and setter
     * */
    @Autowired
    public void setPackageDao(PackageDao packageDao) {
        this.packageDao = packageDao;
    }

    @Autowired
    public void setFileDao(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    @Autowired
    public void setClassDao(ClassDao classDao) {
        this.classDao = classDao;
    }

    @Autowired
    public void setFieldDao(FieldDao fieldDao) {
        this.fieldDao = fieldDao;
    }

    @Autowired
    public void setMethodDao(MethodDao methodDao) {
        this.methodDao = methodDao;
    }

    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface) {
        this.restInterface = restInterface;
    }

    @Autowired
    public void setStatementDao(StatementDao statementDao) {
        this.statementDao = statementDao;
    }
}