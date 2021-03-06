/**
 * @description:
 * @author: fancying
 * @create: 2019-09-25 20:41
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.LineInfo;
import cn.edu.fudan.codetracker.domain.RelationShip;
import cn.edu.fudan.codetracker.core.AnalyzeDiffFile;
import cn.edu.fudan.codetracker.core.MetaInfoAnalysis;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.ScanService;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import cn.edu.fudan.codetracker.util.cldiff.ClDiffHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import cn.edu.fudan.codetracker.util.JavancssScaner;

import java.util.*;

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
    private LineInfoDao lineInfoDao;

    private RestInterfaceManager restInterface;

    @Value("${outputDir}")
    private String outputDir;

    private Map<String,LineInfo> lineInfoMap = new HashMap<>();

    /**
     * 第一次扫描 存储项目结构
     * */
    @Override
    public void firstScan(String repoUuid, String branch, String duration) {
        String repoPath = IS_WINDOWS ? getRepoPathByUuid(repoUuid) : restInterface.getRepoPath(repoUuid);
//        String repoPath = getRepoPathByUuid(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndDuration(branch, duration);
        log.info("commit size : " +  commitList.size());
        boolean isInit = false;
        scanCommitList(repoUuid, branch, repoPath, jGitHelper, commitList, isInit);
        restInterface.freeRepo(repoUuid, repoPath);
    }

    @Override
    public void autoScan(String repoUuid, String branch, String beginCommit) {
        String repoPath = restInterface.getRepoPath(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        String commitId = findScanLatest(repoUuid, branch);
        if (commitId != null) {
            log.error("First Scan Error: this repo has already been scanned!");
            return;
        }
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, false);
        log.info("commit size : " +  commitList.size());
        scanCommitList(repoUuid, branch, repoPath, jGitHelper, commitList, false);
        restInterface.freeRepo(repoUuid, repoPath);
    }

    @Override
    public void autoUpdate(String repoUuid, String branch) {
        String repoPath = restInterface.getRepoPath(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        String commitId = findScanLatest(repoUuid, branch);
        if (commitId == null) {
            log.error("Update Scan Error: this repo hasn't been scanned!");
            return;
        }
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, commitId, true);
        log.info("commit size : " +  commitList.size());
        scanCommitList(repoUuid, branch, repoPath, jGitHelper, commitList, true);
        restInterface.freeRepo(repoUuid, repoPath);
    }

    private void scanCommitList(String repoUuid, String branch, String repoPath, JGitHelper jGitHelper, List<String> commitList, boolean isInit) {
        RepoInfoBuilder repoInfo;
        int num = 0;
        for (String commit : commitList) {
            ++num;
            log.info("start commit：" + num  + "  " + commit);
            if (isInit) {
                scan(repoUuid , commit, branch, jGitHelper, repoPath);
            } else {
                jGitHelper.checkout(commit);
                repoInfo = new RepoInfoBuilder(repoUuid, commit, repoPath, jGitHelper, branch, null, null);
                repoInfo.setCommitter(jGitHelper.getAuthorName(commit));
                saveData(repoInfo);
                isInit = true;
                lineCountFirstScan(repoInfo, repoPath);
            }
        }
    }

    /**
     * 返回库里扫描过最新的commitId
     * @param repoUuid
     * @param branch
     * @return
     */
    private String findScanLatest(String repoUuid, String branch) {
        return packageDao.findScanLatest(repoUuid, branch);
    }

    private void lineCountFirstScan(RepoInfoBuilder repoInfo,String repoPath) {
        LineInfo lineInfo = new LineInfo();
        lineInfo.setImportCount(repoInfo.getImportCount());
        lineInfo.setCommitId(repoInfo.getCommit());
        lineInfo.setCommitter(repoInfo.getCommitter());
        lineInfo.setCommitDate(repoInfo.getBaseInfo().getCommitDate());
        lineInfo.setRepoUuid(repoInfo.getRepoUuid());
        lineInfo.setBranch(repoInfo.getBranch());
        int lineCount = JavancssScaner.scanFile(repoPath) - lineInfo.getImportCount();
        lineInfo.setLineCount(lineCount);
        //first time,all files are added
        lineInfo.setAddCount(lineCount + lineInfo.getImportCount());
        lineInfo.setDeleteCount(0);
        lineInfoMap.put(lineInfo.getCommitId(),lineInfo);
        try {
            lineInfoDao.insertLineInfo(lineInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void lineCountScan(String repoUuid, String commitId, String repoPath, JGitHelper jGitHelper, String branch, MetaInfoAnalysis analysis){
        try {
            jGitHelper.checkout(commitId);
            LineInfo lineInfo = new LineInfo();
            lineInfo.setCommitId(commitId);
            RepoInfoBuilder repoInfo;

            if (analysis.getPreCommitIds().size() > 1) {
                repoInfo = new RepoInfoBuilder(repoUuid, commitId, repoPath, jGitHelper, branch, null, null);
                lineInfo.setImportCount(repoInfo.getImportCount());
                lineInfo.setAddCount(0);
                lineInfo.setDeleteCount(0);
            } else {
                String preCommitId = analysis.getPreCommitIds().get(0);
                repoInfo = new RepoInfoBuilder(repoUuid, commitId, repoPath, jGitHelper, branch, preCommitId, null);
                if (analysis.getCurFileListMap().get(preCommitId).size() == 0 &&
                        analysis.getAddFilesListMap().get(preCommitId).size() == 0 &&
                        analysis.getDeleteFilesListMap().get(preCommitId).size() == 0) {
                    lineInfo.setImportCount(repoInfo.getImportCount());
                    lineInfo.setAddCount(0);
                    lineInfo.setDeleteCount(0);
                } else {
                    int preImportCount = lineInfoMap.get(preCommitId).getImportCount();
                    lineInfo.setImportCount(preImportCount + analysis.getChangeImportCount());

                    int addCount = 0;
                    int deleteCount = 0;
                    int changeCount = 0;

                    for (String addPath : analysis.getAddFilesListMap().get(preCommitId)) {
                        addCount += JavancssScaner.scanOneFile(addPath);
                    }

                    for (String deletePath : analysis.getDeleteFilesListMap().get(preCommitId)) {
                        deleteCount += JavancssScaner.scanOneFile(deletePath);
                    }

                    for (String curPath : analysis.getCurFileListMap().get(preCommitId)) {
                        changeCount += JavancssScaner.scanOneFile(curPath);
                    }

                    for (String prePath : analysis.getPreFileListMap().get(preCommitId)) {
                        changeCount -= JavancssScaner.scanOneFile(prePath);
                    }

                    if (changeCount >= 0) {
                        addCount += changeCount;
                    } else {
                        deleteCount += (-changeCount);
                    }

                    lineInfo.setAddCount(addCount);
                    lineInfo.setDeleteCount(deleteCount);
                }
            }
            lineInfo.setCommitter(repoInfo.getCommitter());
            lineInfo.setCommitDate(repoInfo.getBaseInfo().getCommitDate());
            lineInfo.setRepoUuid(repoInfo.getRepoUuid());
            lineInfo.setBranch(repoInfo.getBranch());

            int lineCount = JavancssScaner.scanFile(repoPath) - lineInfo.getImportCount();
            lineInfo.setLineCount(lineCount);

            lineInfoMap.put(lineInfo.getCommitId(),lineInfo);

            lineInfoDao.insertLineInfo(lineInfo);
        } catch (Exception e) {
            e.printStackTrace();
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
        MetaInfoAnalysis analysis = new MetaInfoAnalysis(repoUuid, branch, outputPath, jGitHelper, commitId);
        List<AnalyzeDiffFile> analyzeDiffFiles = analysis.analyzeMetaInfo(new ProxyDao(packageDao, fileDao, classDao, fieldDao, methodDao, statementDao));
        lineCountScan(repoUuid, commitId, repoPath, jGitHelper, branch, analysis);
        if (analysis.getMergeNum() != JGitHelper.getNotMerge()) {
            return;
        }
        // 扫描结果记录入库
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
            return IS_WINDOWS  ? "E:\\Lab\\iec-wepm-develop" :"/Users/tangyuan/Documents/Git/iec-wepm-develop";
        }

        return "/Users/tangyuan/Documents/Git/IssueTracker-Master";
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

    @Autowired
    public void setLineInfoDao(LineInfoDao lineInfoDao) { this.lineInfoDao = lineInfoDao; }
    
}