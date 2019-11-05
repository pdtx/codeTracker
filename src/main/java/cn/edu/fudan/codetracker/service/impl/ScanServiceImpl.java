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
import cn.edu.fudan.codetracker.util.cldiff.CLDiffHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScanServiceImpl implements ScanService {

    private PackageDao packageDao;
    private FileDao fileDao;
    private ClassDao classDao;
    private FieldDao fieldDao;
    private MethodDao methodDao;

    private RestInterfaceManager restInterface;

    @Value("outputDir")
    private String outputDir;

    /**
     * 第一次扫描 存储项目结构
     * */
    @Override
    public void firstScan(String repoUuid, String branch, String duration) {
        //String repoPath = restInterface.getRepoPath(repoUuid);
        String repoPath = getRepoPathByUuid(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndDuration(branch, duration);
        RepoInfoBuilder repoInfo;
        boolean isInit = false;
        for (String commit : commitList) {
            if (isInit) {
                scan(repoUuid , commit, branch, outputDir, jGitHelper);
            } else {
                repoInfo = new RepoInfoBuilder(repoUuid, commit, repoPath, jGitHelper, branch, null);
                repoInfo.setCommitter(jGitHelper.getAuthorName(commit));
                saveData(repoInfo);
                isInit = true;
            }
        }
    }

    @Override
    public Object getMethodHistory(String repoId, String moduleName, String packageName, String className, String signature) {
        return methodDao.getMethodHistory(repoId, moduleName, packageName, className, signature);
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
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 后续扫描分析 diff 结果
     * */
    @Override
    public boolean scan(String repoUuid, String commitId, String branch, String outputDir, JGitHelper jGitHelper) {

        String repoPath = getRepoPathByUuid(repoUuid);

        if (jGitHelper != null) {
            jGitHelper = new JGitHelper(repoPath);
        }

        // 分析版本之间的关系
        CLDiffHelper.executeCLDiff(repoPath, commitId, outputDir);
        String [] path = repoPath.replace('\\','/').split("/");
        String outputPath = outputDir + "\\" + path[path.length -1];
        // extra diff info and construct tracking relation
        OutputAnalysis analysis = new OutputAnalysis(repoUuid, branch, outputPath, jGitHelper, commitId);
        List<AnalyzeDiffFile> analyzeDiffFiles = analysis.analyzeMetaInfo(packageDao, fileDao, classDao, fieldDao, methodDao);
        // 扫描结果记录入库
        try {

            for (AnalyzeDiffFile analyzeDiffFile : analyzeDiffFiles) {

                //add
                packageDao.setAddInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.ADD.name()));
                fileDao.setAddInfo(analyzeDiffFile.getFileInfos().get(RelationShip.ADD.name()));
                classDao.setAddInfo(analyzeDiffFile.getClassInfos().get(RelationShip.ADD.name()));
                methodDao.setAddInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.ADD.name()));
                fieldDao.setAddInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.ADD.name()));
                //delete
                packageDao.setDeleteInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.DELETE.name()));
                fileDao.setDeleteInfo(analyzeDiffFile.getFileInfos().get(RelationShip.DELETE.name()));
                classDao.setDeleteInfo(analyzeDiffFile.getClassInfos().get(RelationShip.DELETE.name()));
                methodDao.setDeleteInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.DELETE.name()));
                fieldDao.setDeleteInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.DELETE.name()));
                //change
                packageDao.setChangeInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.CHANGE.name()));
                fileDao.setChangeInfo(analyzeDiffFile.getFileInfos().get(RelationShip.CHANGE.name()));
                classDao.setChangeInfo(analyzeDiffFile.getClassInfos().get(RelationShip.CHANGE.name()));
                methodDao.setChangeInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.CHANGE.name()));
                fieldDao.setChangeInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.CHANGE.name()));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void firstScan(String repoUuid, List<String> commitList, String branch) {
        scan(repoUuid, commitList, branch);
    }

    public void scan(String repoUuid, List<String> commitList, String branch) {
        String repoPath = getRepoPathByUuid(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        RepoInfoBuilder repoInfo;
        if (commitList.size() >= 1) {
            repoInfo = new RepoInfoBuilder(repoUuid, commitList.get(0), repoPath, jGitHelper, branch, null);
            repoInfo.setCommitter(jGitHelper.getAuthorName(commitList.get(0)));
            // 入库
            saveData(repoInfo);
        }
        //String [] path = repoPath.replace('\\','/').split("/");
        for (int i = 1; i < commitList.size();i++) {
            String outputDir = "E:\\Lab\\project\\new";
            scan(repoUuid , commitList.get(i), branch, outputDir, jGitHelper);
        }
    }

    private String getRepoPathByUuid(String repoUuid) {

        return "E:\\Lab\\project\\IssueTracker-Master";
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
}