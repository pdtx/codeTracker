/**
 * @description:
 * @author: fancying
 * @create: 2019-09-25 20:41
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.ScanService;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import cn.edu.fudan.codetracker.util.cldiff.CLDiffHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScanServiceImpl implements ScanService {



    private PackageDao packageDao;
    private FileDao fileDao;
    private ClassDao classDao;
    private FieldDao fieldDao;
    private MethodDao methodDao;





    /**
     * 第一次扫描 存储项目结构
     * */
    @Override
    public void firstScan(String repoUuid, List<String> commitList, String branch) {
        String repoPath = getRepoPathByUUID(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        if (commitList.size() >= 1) {
            RepoInfoBuilder repoInfo = new RepoInfoBuilder(repoUuid, commitList.get(0), repoPath, jGitHelper, branch, null);
            repoInfo.setCommitter(jGitHelper.getAuthorName(commitList.get(0)));
            // 入库
            saveData(repoInfo);
        }

        for (int i = 1; i < commitList.size();i++) {
            String outputDir = "";
            scan(repoPath, commitList.get(i), outputDir);
        }
    }

    private void saveData(RepoInfoBuilder repoInfo) {
/*        packageDao.insertPackageInfoList(repoInfo.getPackageInfos());
        packageDao.insertRawPackageInfoList(repoInfo.getPackageInfos());

        fileDao.insertFileInfoList(repoInfo.getFileInfos());
        fileDao.insertRawFileInfoList(repoInfo.getFileInfos());

        classDao.insertClassInfoList(repoInfo.getClassInfos());
        classDao.insertRawClassInfoList(repoInfo.getClassInfos());*/

        methodDao.insertMethodInfoList(repoInfo.getMethodInfos());
        methodDao.insertRawMethodInfoList(repoInfo.getMethodInfos());

        fieldDao.insertFieldInfoList(repoInfo.getFieldInfos());
        fieldDao.insertRawFieldInfoList(repoInfo.getFieldInfos());
    }


    /**
     * 后续扫描分析 diff 结果
     * */
    @Override
    public boolean scan(String repoPath, String commitId, String outputDir) {

        // 分析版本之间的关系
        CLDiffHelper.executeCLDiff(repoPath, commitId, outputDir);

        // 抽取diff信息 建立追踪关系


        // 扫描结果记录入库


        return false;
    }

    private String getRepoPathByUUID(String repoUuid) {
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

}