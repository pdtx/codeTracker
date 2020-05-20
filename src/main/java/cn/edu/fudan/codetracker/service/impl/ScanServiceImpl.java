/**
 * @description:
 * @author: fancying
 * @create: 2019-09-25 20:41
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.constants.ScanStatus;
import cn.edu.fudan.codetracker.core.*;
import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.ScanService;
import cn.edu.fudan.codetracker.util.DirExplorer;
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

    @Async("taskExecutor")
    @Override
    public void scan(String repoUuid, String branch, String beginCommit) {
        if (findScanLatest(repoUuid, branch) != null) {
            log.warn("First Scan: this repo[{}] has already been scanned!", repoUuid);
            return;
        }

        repoDao.insertScanRepo(UUID.randomUUID().toString(), repoUuid, branch, ScanStatus.SCANNING);
//        String repoPath = restInterface.getRepoPath(repoUuid);
        String repoPath = getRepoPathByUuid(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, false);
        log.info("commit size : " +  commitList.size());
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath, jGitHelper, commitList, false);
        repoDao.updateScanStatus(repoUuid, branch, isAbort ? ScanStatus.ABORTED : ScanStatus.SCANNED);
        restInterface.freeRepo(repoUuid, repoPath);
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

    /**
     * FIXME need @Transactional
     */
    private boolean scanCommitList(String repoUuid, String branch, String repoPath, JGitHelper jGitHelper, List<String> commitList, boolean isInit) {
        RepoInfoBuilder repoInfo;
//        Map<String,LineInfo> lineInfoMap = new HashMap<>();
        int num = 0;
        try {
            for (String commit : commitList) {
                log.info("start commit：{} {}" , ++num, commit);
                if (isInit) {
                    scan(repoUuid , commit, branch, jGitHelper, repoPath);
                } else {
                    jGitHelper.checkout(branch);
                    jGitHelper.checkout(commit);
                    repoInfo = new RepoInfoBuilder(repoUuid, commit, repoPath, jGitHelper, branch, null, null);
                    travelAndSetChangeRelation(repoInfo.getPackageInfos());
                    saveData(repoInfo);
                    isInit = true;
                }
                repoDao.updateLatestCommit(repoUuid, branch, commit);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
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


    private void scan (String repoUuid, String commitId, String branch, JGitHelper jGitHelper, String repoPath) {
        if (jGitHelper != null) {
            jGitHelper = new JGitHelper(repoPath);
        }
        //通过jgit拿到file列表
        jGitHelper.checkout(branch);
        jGitHelper.checkout(commitId);
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

    }

    private void extractAndSaveInfo(RepoInfoBuilder preRepoInfo, RepoInfoBuilder curRepoInfo) {
        //抽取需要入库的数据
        Map<String,Set<PackageNode>> packageMap = new HashMap<>();
        packageMap.put("ADD",new HashSet<>());
        packageMap.put("CHANGE",new HashSet<>());
        packageMap.put("DELETE",new HashSet<>());
        Map<String,Set<FileNode>> fileMap = new HashMap<>();
        fileMap.put("ADD",new HashSet<>());
        fileMap.put("CHANGE",new HashSet<>());
        fileMap.put("DELETE",new HashSet<>());
        Map<String,Set<ClassNode>> classMap = new HashMap<>();
        classMap.put("ADD",new HashSet<>());
        classMap.put("CHANGE",new HashSet<>());
        classMap.put("DELETE",new HashSet<>());
        Map<String,Set<MethodNode>> methodMap = new HashMap<>();
        methodMap.put("ADD",new HashSet<>());
        methodMap.put("CHANGE",new HashSet<>());
        methodMap.put("DELETE",new HashSet<>());
        Map<String,Set<FieldNode>> fieldMap = new HashMap<>();
        fieldMap.put("ADD",new HashSet<>());
        fieldMap.put("CHANGE",new HashSet<>());
        fieldMap.put("DELETE",new HashSet<>());
        Map<String,Set<StatementNode>> statementMap = new HashMap<>();
        statementMap.put("ADD",new HashSet<>());
        statementMap.put("CHANGE",new HashSet<>());
        statementMap.put("DELETE",new HashSet<>());

        //preTree上搜索delete情况
        for (PackageNode packageNode: preRepoInfo.getPackageInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(packageNode.getChangeStatus())) {
                packageMap.get("DELETE").add(packageNode);
            }
        }
        for (FileNode fileNode: preRepoInfo.getFileInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(fileNode.getChangeStatus())) {
                fileMap.get("DELETE").add(fileNode);
            }
        }
        for (ClassNode classNode: preRepoInfo.getClassInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(classNode.getChangeStatus())) {
                classMap.get("DELETE").add(classNode);
            }
        }
        for (MethodNode methodNode: preRepoInfo.getMethodInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(methodNode.getChangeStatus())) {
                methodMap.get("DELETE").add(methodNode);
            }
        }
        for (FieldNode fieldNode: preRepoInfo.getFieldInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(fieldNode.getChangeStatus())) {
                fieldMap.get("DELETE").add(fieldNode);
            }
        }
        for (StatementNode statementNode: preRepoInfo.getStatementInfos()) {
            if (BaseNode.ChangeStatus.DELETE.equals(statementNode.getChangeStatus())) {
                statementMap.get("DELETE").add(statementNode);
            }
        }

        //curTree上搜索add change情况
        for (PackageNode packageNode: curRepoInfo.getPackageInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(packageNode.getChangeStatus())) {
                packageMap.get("ADD").add(packageNode);
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(packageNode.getChangeStatus())) {
                packageMap.get("CHANGE").add(packageNode);
            }
        }
        for (FileNode fileNode: curRepoInfo.getFileInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(fileNode.getChangeStatus())) {
                fileMap.get("ADD").add(fileNode);
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(fileNode.getChangeStatus())) {
                fileMap.get("CHANGE").add(fileNode);
            }
        }
        for (ClassNode classNode: curRepoInfo.getClassInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(classNode.getChangeStatus())) {
                classMap.get("ADD").add(classNode);
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(classNode.getChangeStatus())) {
                classMap.get("CHANGE").add(classNode);
            }
        }
        for (MethodNode methodNode: curRepoInfo.getMethodInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(methodNode.getChangeStatus())) {
                methodMap.get("ADD").add(methodNode);
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(methodNode.getChangeStatus())) {
                methodMap.get("CHANGE").add(methodNode);
            }
        }
        for (FieldNode fieldNode: curRepoInfo.getFieldInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(fieldNode.getChangeStatus())) {
                fieldMap.get("ADD").add(fieldNode);
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(fieldNode.getChangeStatus())) {
                fieldMap.get("CHANGE").add(fieldNode);
            }
        }
        for (StatementNode statementNode: curRepoInfo.getStatementInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(statementNode.getChangeStatus())) {
                statementMap.get("ADD").add(statementNode);
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(statementNode.getChangeStatus())) {
                statementMap.get("CHANGE").add(statementNode);
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

    @Autowired
    public void setRepoDao(RepoDao repoDao) {
        this.repoDao = repoDao;
    }


}