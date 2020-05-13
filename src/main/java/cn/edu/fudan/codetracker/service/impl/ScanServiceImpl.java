/**
 * @description:
 * @author: fancying
 * @create: 2019-09-25 20:41
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.core.*;
import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.ScanService;
import cn.edu.fudan.codetracker.util.DirExplorer;
import cn.edu.fudan.codetracker.util.FileFilter;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import cn.edu.fudan.codetracker.util.cldiff.ClDiffHelper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import cn.edu.fudan.codetracker.util.JavancssScaner;

import java.io.File;
import java.io.IOException;
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
    private RepoDao repoDao;



    private RestInterfaceManager restInterface;

    @Value("${outputDir}")
    private String outputDir;

    /**
     * 第一次扫描 存储项目结构
     * */
    @Async("taskExecutor")
    @Override
    public void firstScan(String repoUuid, String branch, String duration) {
        if (findScanLatest(repoUuid, branch) == null) {
            repoDao.insertScanRepo(UUID.randomUUID().toString(), repoUuid, branch, "scanning");
        } else {
            log.error("First Scan Error: this repo has already been scanned!");
            return;
        }
        String repoPath = IS_WINDOWS ? getRepoPathByUuid(repoUuid) : restInterface.getRepoPath(repoUuid);
//        String repoPath = getRepoPathByUuid(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndDuration(branch, duration);
        log.info("commit size : " +  commitList.size());
        boolean isInit = false;
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath, jGitHelper, commitList, isInit);
        if (isAbort) {
            repoDao.updateScanStatus(repoUuid, branch, "aborted");
        } else {
            repoDao.updateScanStatus(repoUuid, branch, "scanned");
        }
        restInterface.freeRepo(repoUuid, repoPath);
    }

    @Async("taskExecutor")
    @Override
    public void scan(String repoUuid, String branch, String beginCommit) {
        if (findScanLatest(repoUuid, branch) == null) {
            repoDao.insertScanRepo(UUID.randomUUID().toString(), repoUuid, branch, "scanning");
        } else {
            log.error("First Scan Error: this repo has already been scanned!");
            return;
        }
//        String repoPath = restInterface.getRepoPath(repoUuid);
        String repoPath = getRepoPathByUuid(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, false);
        log.info("commit size : " +  commitList.size());
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath, jGitHelper, commitList, false);
        if (isAbort) {
            repoDao.updateScanStatus(repoUuid, branch, "aborted");
        } else {
            repoDao.updateScanStatus(repoUuid, branch, "scanned");
        }
//        restInterface.freeRepo(repoUuid, repoPath);
    }

    @Async("taskExecutor")
    @Override
    public void autoUpdate(String repoUuid, String branch) {
        String commitId = findScanLatest(repoUuid, branch);
        if (commitId == null) {
            log.error("Update Scan Error: this repo hasn't been scanned!");
            return;
        }
        repoDao.updateScanStatus(repoUuid, branch, "scanning");
        String repoPath = restInterface.getRepoPath(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, commitId, true);
        log.info("commit size : " +  commitList.size());
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath, jGitHelper, commitList, true);
        if (isAbort) {
            repoDao.updateScanStatus(repoUuid, branch, "aborted");
        } else {
            repoDao.updateScanStatus(repoUuid, branch, "scanned");
        }
        restInterface.freeRepo(repoUuid, repoPath);
    }

    private boolean scanCommitList(String repoUuid, String branch, String repoPath, JGitHelper jGitHelper, List<String> commitList, boolean isInit) {
        RepoInfoBuilder repoInfo;
//        Map<String,LineInfo> lineInfoMap = new HashMap<>();
        int num = 0;
        try {
            for (String commit : commitList) {
                ++num;
                log.info("start commit：" + num  + "  " + commit);
                if (isInit) {
                    scan(repoUuid , commit, branch, jGitHelper, repoPath);
                } else {
                    jGitHelper.checkout(commit);
                    repoInfo = new RepoInfoBuilder(repoUuid, commit, repoPath, jGitHelper, branch, null, null);
//                    repoInfo.setCommitter(jGitHelper.getAuthorName(commit));
                    travelAndSetChangeRelation(repoInfo.getPackageInfos());
                    saveData(repoInfo);
                    isInit = true;
//                    lineCountFirstScan(repoInfo, repoPath, lineInfoMap);
                }
                repoDao.updateLatestCommit(repoUuid, branch, commit);
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return true;
        }
    }


    private void travelAndSetChangeRelation(List<? extends BaseNode> baseNodes){
        if (baseNodes == null) {
            return;
        }
        for (BaseNode baseNode : baseNodes) {
            baseNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
            travelAndSetChangeRelation(baseNode.getChildren());
            if (baseNode instanceof ClassNode) {
                ClassNode classNode = (ClassNode)baseNode;
                travelAndSetChangeRelation(classNode.getFieldNodes());
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
        return repoDao.getLatestScan(repoUuid, branch);
    }

    @Override
    public String getScanStatus(String repoId, String branch) {
        return repoDao.getScanStatus(repoId, branch);
    }

//    private void lineCountFirstScan(RepoInfoBuilder repoInfo,String repoPath,Map<String,LineInfo> lineInfoMap) {
//        LineInfo lineInfo = new LineInfo();
//        lineInfo.setImportCount(repoInfo.getImportCount());
//        lineInfo.setCommitId(repoInfo.getCommit());
//        lineInfo.setCommitter(repoInfo.getCommitter());
//        lineInfo.setCommitDate(repoInfo.getBaseInfo().getCommitDate());
//        lineInfo.setRepoUuid(repoInfo.getRepoUuid());
//        lineInfo.setBranch(repoInfo.getBranch());
//        int lineCount = JavancssScaner.scanFile(repoPath) - lineInfo.getImportCount();
//        lineInfo.setLineCount(lineCount);
//        //first time,all files are added
//        lineInfo.setAddCount(lineCount + lineInfo.getImportCount());
//        lineInfo.setDeleteCount(0);
//        lineInfoMap.put(lineInfo.getCommitId(),lineInfo);
//        try {
//            lineInfoDao.insertLineInfo(lineInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private void lineCountScan(String repoUuid, String commitId, String repoPath, JGitHelper jGitHelper, String branch, MetaInfoAnalysis analysis,Map<String,LineInfo> lineInfoMap){
//        try {
//            jGitHelper.checkout(commitId);
//            LineInfo lineInfo = new LineInfo();
//            lineInfo.setCommitId(commitId);
//            RepoInfoBuilder repoInfo;
//
//            if (analysis.getPreCommitIds().size() > 1) {
//                repoInfo = new RepoInfoBuilder(repoUuid, commitId, repoPath, jGitHelper, branch, null, null);
//                lineInfo.setImportCount(repoInfo.getImportCount());
//                lineInfo.setAddCount(0);
//                lineInfo.setDeleteCount(0);
//            } else {
//                String preCommitId = analysis.getPreCommitIds().get(0);
//                repoInfo = new RepoInfoBuilder(repoUuid, commitId, repoPath, jGitHelper, branch, preCommitId, null);
//                if (analysis.getCurFileListMap().get(preCommitId).size() == 0 &&
//                        analysis.getAddFilesListMap().get(preCommitId).size() == 0 &&
//                        analysis.getDeleteFilesListMap().get(preCommitId).size() == 0) {
//                    lineInfo.setImportCount(repoInfo.getImportCount());
//                    lineInfo.setAddCount(0);
//                    lineInfo.setDeleteCount(0);
//                } else {
//                    if (lineInfoMap.get(preCommitId) == null) {
//                        lineInfo.setImportCount(repoInfo.getImportCount());
//                    } else {
//                        int preImportCount = lineInfoMap.get(preCommitId).getImportCount();
//                        lineInfo.setImportCount(preImportCount + analysis.getChangeImportCount());
//                    }
//
//                    int addCount = 0;
//                    int deleteCount = 0;
//                    int changeCount = 0;
//
//                    for (String addPath : analysis.getAddFilesListMap().get(preCommitId)) {
//                        addCount += JavancssScaner.scanOneFile(addPath);
//                    }
//
//                    for (String deletePath : analysis.getDeleteFilesListMap().get(preCommitId)) {
//                        deleteCount += JavancssScaner.scanOneFile(deletePath);
//                    }
//
//                    for (String curPath : analysis.getCurFileListMap().get(preCommitId)) {
//                        changeCount += JavancssScaner.scanOneFile(curPath);
//                    }
//
//                    for (String prePath : analysis.getPreFileListMap().get(preCommitId)) {
//                        changeCount -= JavancssScaner.scanOneFile(prePath);
//                    }
//
//                    if (changeCount >= 0) {
//                        addCount += changeCount;
//                    } else {
//                        deleteCount += (-changeCount);
//                    }
//
//                    lineInfo.setAddCount(addCount);
//                    lineInfo.setDeleteCount(deleteCount);
//                }
//            }
//            lineInfo.setCommitter(repoInfo.getCommitter());
//            lineInfo.setCommitDate(repoInfo.getBaseInfo().getCommitDate());
//            lineInfo.setRepoUuid(repoInfo.getRepoUuid());
//            lineInfo.setBranch(repoInfo.getBranch());
//
//            int lineCount = JavancssScaner.scanFile(repoPath) - lineInfo.getImportCount();
//            lineInfo.setLineCount(lineCount);
//
//            lineInfoMap.put(lineInfo.getCommitId(),lineInfo);
//
//            lineInfoDao.insertLineInfo(lineInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    private void scan (String repoUuid, String commitId, String branch, JGitHelper jGitHelper, String repoPath) {
        if (jGitHelper != null) {
            jGitHelper = new JGitHelper(repoPath);
        }
        //通过jgit拿到file列表
        jGitHelper.checkout(branch);
        Map<String,Map<String, List<String>>> fileMap = jGitHelper.getFileList(commitId);

        //merge情况直接跳过
        if (fileMap.keySet().size() > 1) {
            return;
        }

        for (String s : fileMap.keySet()) {
            String preCommit = s;
            Map<String, List<String>> map = fileMap.get(preCommit);

            //根据file列表构建preTree和curTree
            List<String> preRelatives = new ArrayList<>();
            List<String> curRelatives = new ArrayList<>();
            List<String> preFileList;
            List<String> curFileList;
            preRelatives.addAll(map.get("CHANGE"));
            preRelatives.addAll(map.get("DELETE"));
            preFileList = localizeFilePath(repoPath, preRelatives);
            curRelatives.addAll(map.get("CHANGE"));
            curRelatives.addAll(map.get("ADD"));
            curFileList = localizeFilePath(repoPath, curRelatives);
            //String repoUuid, String commit, List<String> fileList, JGitHelper jGitHelper, String branch, String parentCommit, List<String> relativePath
            RepoInfoBuilder preRepoInfo = new RepoInfoBuilder(repoUuid,preCommit,preFileList,jGitHelper,branch,null,preRelatives);
            RepoInfoBuilder curRepoInfo = new RepoInfoBuilder(repoUuid,commitId,curFileList,jGitHelper,branch,preCommit,curRelatives);

            //通过ClDiff拿到逻辑修改文件
            String [] paths = repoPath.replace('\\','/').split("/");
            String outputPath = outputDir +  (IS_WINDOWS ?  "\\" : "/") + paths[paths.length -1] + (IS_WINDOWS ?  "\\" : "/") + commitId;
            Map<String,Map<String,String>> logicalChangedFileMap = extractDiffFilePathFromClDiff(repoPath,commitId,outputPath);

            TrackerCore.mapping(preRepoInfo,curRepoInfo,repoUuid,branch,map,logicalChangedFileMap,outputPath,preCommit);

            extractAndSaveInfo(preRepoInfo,curRepoInfo);


        }



//        // 分析版本之间的关系
//        ClDiffHelper.executeDiff(repoPath, commitId, outputDir);
//        String [] path = repoPath.replace('\\','/').split("/");
//        String outputPath = outputDir +  (IS_WINDOWS ?  "\\" : "/") + path[path.length -1];
//        // extra diff info and construct tracking relation
//        MetaInfoAnalysis analysis = new MetaInfoAnalysis(repoUuid, branch, outputPath, jGitHelper, commitId);
//        List<AnalyzeDiffFile> analyzeDiffFiles = analysis.analyzeMetaInfo(new ProxyDao(packageDao, fileDao, classDao, fieldDao, methodDao, statementDao));
//        lineCountScan(repoUuid, commitId, repoPath, jGitHelper, branch, analysis, lineInfoMap);
//        if (analysis.getMergeNum() != JGitHelper.getNotMerge()) {
//            return;
//        }
//        // 扫描结果记录入库
//        for (AnalyzeDiffFile analyzeDiffFile : analyzeDiffFiles) {
//            //add
////            packageDao.setAddInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.ADD.name()));
////            fileDao.setAddInfo(analyzeDiffFile.getFileInfos().get(RelationShip.ADD.name()));
////            classDao.setAddInfo(analyzeDiffFile.getClassInfos().get(RelationShip.ADD.name()));
////            methodDao.setAddInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.ADD.name()));
////            fieldDao.setAddInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.ADD.name()));
////            statementDao.setAddInfo(analyzeDiffFile.getStatementInfos().get(RelationShip.ADD.name()));
////            //delete
////            packageDao.setDeleteInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.DELETE.name()));
////            fileDao.setDeleteInfo(analyzeDiffFile.getFileInfos().get(RelationShip.DELETE.name()));
////            classDao.setDeleteInfo(analyzeDiffFile.getClassInfos().get(RelationShip.DELETE.name()));
////            methodDao.setDeleteInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.DELETE.name()));
////            fieldDao.setDeleteInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.DELETE.name()));
////            statementDao.setDeleteInfo(analyzeDiffFile.getStatementInfos().get(RelationShip.DELETE.name()));
////            //change
////            packageDao.setChangeInfo(analyzeDiffFile.getPackageInfos().get(RelationShip.CHANGE.name()));
////            fileDao.setChangeInfo(analyzeDiffFile.getFileInfos().get(RelationShip.CHANGE.name()));
////            classDao.setChangeInfo(analyzeDiffFile.getClassInfos().get(RelationShip.CHANGE.name()));
////            methodDao.setChangeInfo(analyzeDiffFile.getMethodInfos().get(RelationShip.CHANGE.name()));
////            fieldDao.setChangeInfo(analyzeDiffFile.getFieldInfos().get(RelationShip.CHANGE.name()));
////            statementDao.setChangeInfo(analyzeDiffFile.getStatementInfos().get(RelationShip.CHANGE.name()));
//        }

    }

    private void extractAndSaveInfo(RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo) {
        //抽取需要入库的数据
        Map<String,Set<PackageNode>> packageMap = new HashMap<>();
        Map<String,Set<FileNode>> fileMap = new HashMap<>();
        Map<String,Set<ClassNode>> classMap = new HashMap<>();
        Map<String,Set<MethodNode>> methodMap = new HashMap<>();
        Map<String,Set<FieldNode>> fieldMap = new HashMap<>();
        Map<String,Set<StatementNode>> statementMap = new HashMap<>();

        //preTree上搜索delete情况
        for (PackageNode packageNode: preRepoInfo.getPackageInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(packageNode.getChangeStatus())) {
                if (packageMap.keySet().contains("DELETE")) {
                    packageMap.get("DELETE").add(packageNode);
                } else {
                    Set<PackageNode> set = new HashSet<>();
                    set.add(packageNode);
                    packageMap.put("DELETE",set);
                }
            }
        }
        for (FileNode fileNode: preRepoInfo.getFileInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(fileNode.getChangeStatus())) {
                if (fileMap.keySet().contains("DELETE")) {
                    fileMap.get("DELETE").add(fileNode);
                } else {
                    Set<FileNode> set = new HashSet<>();
                    set.add(fileNode);
                    fileMap.put("DELETE",set);
                }
            }
        }
        for (ClassNode classNode: preRepoInfo.getClassInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(classNode.getChangeStatus())) {
                if (classMap.keySet().contains("DELETE")) {
                    classMap.get("DELETE").add(classNode);
                } else {
                    Set<ClassNode> set = new HashSet<>();
                    set.add(classNode);
                    classMap.put("DELETE",set);
                }
            }
        }
        for (MethodNode methodNode: preRepoInfo.getMethodInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(methodNode.getChangeStatus())) {
                if (methodMap.keySet().contains("DELETE")) {
                    methodMap.get("DELETE").add(methodNode);
                } else {
                    Set<MethodNode> set = new HashSet<>();
                    set.add(methodNode);
                    methodMap.put("DELETE",set);
                }
            }
        }
        for (FieldNode fieldNode: preRepoInfo.getFieldInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(fieldNode.getChangeStatus())) {
                if (fieldMap.keySet().contains("DELETE")) {
                    fieldMap.get("DELETE").add(fieldNode);
                } else {
                    Set<FieldNode> set = new HashSet<>();
                    set.add(fieldNode);
                    fieldMap.put("DELETE",set);
                }
            }
        }
        for (StatementNode statementNode: preRepoInfo.getStatementInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(statementNode.getChangeStatus())) {
                if (statementMap.keySet().contains("DELETE")) {
                    statementMap.get("DELETE").add(statementNode);
                } else {
                    Set<StatementNode> set = new HashSet<>();
                    set.add(statementNode);
                    statementMap.put("DELETE",set);
                }
            }
        }

        //curTree上搜索add change情况
        for (PackageNode packageNode: curRepoInfo.getPackageInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(packageNode.getChangeStatus())) {
                if (packageMap.keySet().contains("ADD")) {
                    packageMap.get("ADD").add(packageNode);
                } else {
                    Set<PackageNode> set = new HashSet<>();
                    set.add(packageNode);
                    packageMap.put("ADD",set);
                }
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(packageNode.getChangeStatus())) {
                if (packageMap.keySet().contains("CHANGE")) {
                    packageMap.get("CHANGE").add(packageNode);
                } else {
                    Set<PackageNode> set = new HashSet<>();
                    set.add(packageNode);
                    packageMap.put("CHANGE",set);
                }
            }
        }
        for (FileNode fileNode: curRepoInfo.getFileInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(fileNode.getChangeStatus())) {
                if (fileMap.keySet().contains("ADD")) {
                    fileMap.get("ADD").add(fileNode);
                } else {
                    Set<FileNode> set = new HashSet<>();
                    set.add(fileNode);
                    fileMap.put("ADD",set);
                }
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(fileNode.getChangeStatus())) {
                if (fileMap.keySet().contains("CHANGE")) {
                    fileMap.get("CHANGE").add(fileNode);
                } else {
                    Set<FileNode> set = new HashSet<>();
                    set.add(fileNode);
                    fileMap.put("CHANGE",set);
                }
            }
        }
        for (ClassNode classNode: curRepoInfo.getClassInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(classNode.getChangeStatus())) {
                if (classMap.keySet().contains("ADD")) {
                    classMap.get("ADD").add(classNode);
                } else {
                    Set<ClassNode> set = new HashSet<>();
                    set.add(classNode);
                    classMap.put("ADD",set);
                }
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(classNode.getChangeStatus())) {
                if (classMap.keySet().contains("CHANGE")) {
                    classMap.get("CHANGE").add(classNode);
                } else {
                    Set<ClassNode> set = new HashSet<>();
                    set.add(classNode);
                    classMap.put("CHANGE",set);
                }
            }
        }
        for (MethodNode methodNode: curRepoInfo.getMethodInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(methodNode.getChangeStatus())) {
                if (methodMap.keySet().contains("ADD")) {
                    methodMap.get("ADD").add(methodNode);
                } else {
                    Set<MethodNode> set = new HashSet<>();
                    set.add(methodNode);
                    methodMap.put("ADD",set);
                }
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(methodNode.getChangeStatus())) {
                if (methodMap.keySet().contains("CHANGE")) {
                    methodMap.get("CHANGE").add(methodNode);
                } else {
                    Set<MethodNode> set = new HashSet<>();
                    set.add(methodNode);
                    methodMap.put("CHANGE",set);
                }
            }
        }
        for (FieldNode fieldNode: curRepoInfo.getFieldInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(fieldNode.getChangeStatus())) {
                if (fieldMap.keySet().contains("ADD")) {
                    fieldMap.get("ADD").add(fieldNode);
                } else {
                    Set<FieldNode> set = new HashSet<>();
                    set.add(fieldNode);
                    fieldMap.put("ADD",set);
                }
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(fieldNode.getChangeStatus())) {
                if (fieldMap.keySet().contains("CHANGE")) {
                    fieldMap.get("CHANGE").add(fieldNode);
                } else {
                    Set<FieldNode> set = new HashSet<>();
                    set.add(fieldNode);
                    fieldMap.put("CHANGE",set);
                }
            }
        }
        for (StatementNode statementNode: curRepoInfo.getStatementInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(statementNode.getChangeStatus())) {
                if (statementMap.keySet().contains("ADD")) {
                    statementMap.get("ADD").add(statementNode);
                } else {
                    Set<StatementNode> set = new HashSet<>();
                    set.add(statementNode);
                    statementMap.put("ADD",set);
                }
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(statementNode.getChangeStatus())) {
                if (statementMap.keySet().contains("CHANGE")) {
                    statementMap.get("CHANGE").add(statementNode);
                } else {
                    Set<StatementNode> set = new HashSet<>();
                    set.add(statementNode);
                    statementMap.put("CHANGE",set);
                }
            }
        }

        save(packageMap,fileMap,classMap,methodMap,fieldMap,statementMap,curRepoInfo.getCommonInfo());
    }

    private void save(Map<String,Set<PackageNode>> packageMap,Map<String,Set<FileNode>> fileMap,Map<String,Set<ClassNode>> classMap,Map<String,Set<MethodNode>> methodMap,Map<String,Set<FieldNode>> fieldMap,Map<String,Set<StatementNode>> statementMap,CommonInfo commonInfo) {
        //入库
        try {
            //add
            packageDao.setAddInfo(packageMap.get("ADD"),commonInfo);
            fileDao.setAddInfo(fileMap.get("ADD"),commonInfo);
            classDao.setAddInfo(classMap.get("ADD"),commonInfo);
            methodDao.setAddInfo(methodMap.get("ADD"),commonInfo);
            fieldDao.setAddInfo(fieldMap.get("ADD"),commonInfo);
            statementDao.setAddInfo(statementMap.get("ADD"),commonInfo);
            //delete
            packageDao.setDeleteInfo(packageMap.get("DELETE"),commonInfo);
            fileDao.setDeleteInfo(fileMap.get("DELETE"),commonInfo);
            classDao.setDeleteInfo(classMap.get("DELETE"),commonInfo);
            methodDao.setDeleteInfo(methodMap.get("DELETE"),commonInfo);
            fieldDao.setDeleteInfo(fieldMap.get("DELETE"),commonInfo);
            statementDao.setDeleteInfo(statementMap.get("DELETE"),commonInfo);
            //change
            packageDao.setChangeInfo(packageMap.get("CHANGE"),commonInfo);
            fileDao.setChangeInfo(fileMap.get("CHANGE"),commonInfo);
            classDao.setChangeInfo(classMap.get("CHANGE"),commonInfo);
            methodDao.setChangeInfo(methodMap.get("CHANGE"),commonInfo);
            fieldDao.setChangeInfo(fieldMap.get("CHANGE"),commonInfo);
            statementDao.setChangeInfo(statementMap.get("CHANGE"),commonInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Map<String,Map<String,String>> extractDiffFilePathFromClDiff(String repoPath,String commitId,String outputPath) {
        Map<String,Map<String,String>> map = new HashMap<>();
        ClDiffHelper.executeDiff(repoPath,commitId,outputDir);
        File file = new File(outputPath);
        String metaPath = findMetaFile(file);
        if (metaPath == null || metaPath.length() == 0) {
            return map;
        }

        JSONArray fileInfoJsonArray;
        JSONArray preCommits;
        String input ;
        try {
            input = FileUtils.readFileToString(new File(metaPath), "UTF-8");
        } catch (IOException e) {
            log.error("meta file path can not find:{}", metaPath);
            return map;
        }

        JSONObject metaInfoJson = JSONObject.parseObject(input);
        preCommits = metaInfoJson.getJSONArray("parents");
        fileInfoJsonArray = metaInfoJson.getJSONArray("files");
        JSONArray actions = metaInfoJson.getJSONArray("actions");

        for (int i = 0; i < preCommits.size(); i++) {
            String preCommit = preCommits.getString(i);
            Map<String,String> fileMap = new HashMap<>();
            for (int j = 0; j < fileInfoJsonArray.size(); j++) {
                JSONObject jsonObject = fileInfoJsonArray.getJSONObject(j);
                if (preCommit.equals(jsonObject.getString("parent_commit"))) {
                    if (jsonObject.getString("diffPath") != null && "MODIFY".equals(actions.getString(j))) {
                        fileMap.put(jsonObject.getString("file_full_name"),jsonObject.getString("diffPath"));
                    }
                }
            }
            map.put(preCommit,fileMap);
        }

        return map;

    }

    private String findMetaFile(File projectDir) {
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith("meta.json"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        if (pathList.size() == 0) {
            return null;
        }
        return pathList.get(0);
    }

    //将文件相对路径转成绝对路径
    private List<String> localizeFilePath(String repoPath, List<String> filePath) {
        for (int i = 0; i < filePath.size() ; i++) {
            filePath.set(i, repoPath + "/" + filePath.get(i));
        }
        return filePath;
    }

    private void saveData(RepoInfoBuilder repoInfo) {
        try {

            packageDao.insertPackageInfoList(repoInfo.getPackageInfos(),repoInfo.getCommonInfo());
            packageDao.insertRawPackageInfoList(repoInfo.getPackageInfos(),repoInfo.getCommonInfo());

            fileDao.insertFileInfoList(repoInfo.getFileInfos(),repoInfo.getCommonInfo());
            fileDao.insertRawFileInfoList(repoInfo.getFileInfos(),repoInfo.getCommonInfo());

            classDao.insertClassInfoList(repoInfo.getClassInfos(),repoInfo.getCommonInfo());
            classDao.insertRawClassInfoList(repoInfo.getClassInfos(),repoInfo.getCommonInfo());

            methodDao.insertMethodInfoList(repoInfo.getMethodInfos(),repoInfo.getCommonInfo());
            methodDao.insertRawMethodInfoList(repoInfo.getMethodInfos(),repoInfo.getCommonInfo());

            fieldDao.insertFieldInfoList(repoInfo.getFieldInfos(),repoInfo.getCommonInfo());
            fieldDao.insertRawFieldInfoList(repoInfo.getFieldInfos(),repoInfo.getCommonInfo());

            statementDao.insertStatementInfoList(repoInfo.getStatementInfos(),repoInfo.getCommonInfo());
            statementDao.insertRawStatementInfoList(repoInfo.getStatementInfos(),repoInfo.getCommonInfo());
            statementDao.insertStatementRelationList(repoInfo.getStatementInfos(),repoInfo.getCommonInfo());

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

//    @Autowired
//    public void setLineInfoDao(LineInfoDao lineInfoDao) { this.lineInfoDao = lineInfoDao; }

    @Autowired
    public void setRepoDao(RepoDao repoDao) {
        this.repoDao = repoDao;
    }


    public static void main(String[] args) {
        String repoUuid = "Issue";
        String repoPath = "/Users/tangyuan/Documents/Git/IssueTracker-Master";
        String branch = "zhonghui20191012";
        String commit = "bccc95d2f36a3392d0f746e4fea4399da6397e15";
        new ScanServiceImpl().scan(repoUuid,commit,branch,new JGitHelper(repoPath),repoPath);
//        new ScanServiceImpl().extractDiffFilePathFromClDiff(repoPath,commit);
    }
}