package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.constants.ScanStatus;
import cn.edu.fudan.codetracker.core.*;
import cn.edu.fudan.codetracker.core.tree.JavaTree;
import cn.edu.fudan.codetracker.core.tree.Language;
import cn.edu.fudan.codetracker.core.tree.RepoInfoTree;
import cn.edu.fudan.codetracker.core.tree.parser.DependencyAnalysis;
import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.ScanService;
import cn.edu.fudan.codetracker.util.DirExplorer;
import cn.edu.fudan.codetracker.util.cldiff.ClDiffHelper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * description 扫描服务
 * @author fancying
 * create 2019-09-25 20:41
 **/
@Slf4j
@Service
public class ScanServiceImpl implements ScanService, PublicConstants {

    private static ThreadLocal<String> repoPath = new ThreadLocal<>();

    private static MergeHandler mergeHandler = MergeHandler.getInstance();

    private PackageDao packageDao;
    private FileDao fileDao;
    private ClassDao classDao;
    private FieldDao fieldDao;
    private MethodDao methodDao;
    private StatementDao statementDao;
    private RepoDao repoDao;
    private MethodCallDao methodCallDao;

    private RestInterfaceManager restInterface;

    @Value("${outputDir}")
    private String outputDir;

    @Async("taskExecutor")
    @Override
    public void scan(String repoUuid, String branch, String beginCommit) {
//        repoPath.set(restInterface.getCodeServiceRepo(repoUuid));
        repoPath.set(getRepoPathByUuid(repoUuid));
        JGitHelper jGitHelper = new JGitHelper(repoPath.get());
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, false);
        log.info("commit size : " +  commitList.size());
        ScanInfo scanInfo = new ScanInfo(UUID.randomUUID().toString(),ScanStatus.SCANNING,commitList.size(),0,new Date(),repoUuid,branch);
        repoDao.insertScanRepo(scanInfo);
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath.get(), jGitHelper, commitList, false, scanInfo);
        scanInfo.setStatus(isAbort ? ScanStatus.FAILED : ScanStatus.COMPLETE);
        repoDao.saveScanInfo(scanInfo);
//        restInterface.freeRepo(repoUuid, repoPath.get());
    }

    @Async("taskExecutor")
    @Override
    public void autoUpdate(String repoUuid, String branch, String commitId) {
//        repoPath.set(restInterface.getCodeServiceRepo(repoUuid));
        repoPath.set(getRepoPathByUuid(repoUuid));
        JGitHelper jGitHelper = new JGitHelper(repoPath.get());
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, commitId, true);
        log.info("commit size : " +  commitList.size());
        ScanInfo scanInfo = new ScanInfo(UUID.randomUUID().toString(),ScanStatus.SCANNING,commitList.size(),0,new Date(),repoUuid,branch);
        repoDao.insertScanRepo(scanInfo);
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath.get(), jGitHelper, commitList, true, scanInfo);
        scanInfo.setStatus(isAbort ? ScanStatus.FAILED : ScanStatus.COMPLETE);
        repoDao.saveScanInfo(scanInfo);
//        restInterface.freeRepo(repoUuid, repoPath.get());
    }

    /**
     * FIXME need @Transactional
     */
    private boolean scanCommitList(String repoUuid, String branch, String repoPath, JGitHelper jGitHelper, List<String> commitList, boolean isUpdate, ScanInfo scanInfo) {
        int num = 0;
        try {
            for (String commit : commitList) {
                log.info("start commit：{} {}" , ++num, commit);
                if (isUpdate) {
                    scan(repoUuid , commit, branch, jGitHelper, repoPath);
                } else {
                    jGitHelper.checkout(branch);
                    jGitHelper.checkout(commit);
                    DependencyAnalysis.setRepoPathT(repoPath);
                    CommonInfo commonInfo = constructCommonInfo(repoUuid,branch,commit,null,jGitHelper);
                    File file = new File(repoPath);
                    //List<String> fileList, CommonInfo commonInfo, String repoUuid
                    RepoInfoTree repoInfoTree = new RepoInfoTree(listFiles(file),commonInfo,repoUuid);
                    JavaTree javaTree = (JavaTree) repoInfoTree.getRepoTree().get(Language.JAVA);
                    travelAndSetChangeRelation(javaTree.getPackageInfos());
                    saveData(javaTree,commonInfo);
                    dealWithMethodCalls(javaTree, commonInfo);
                    isUpdate = true;
                }
                scanInfo.setEndScanTime(new Date());
                scanInfo.setScanTime((scanInfo.getEndScanTime().getTime() - scanInfo.getStartScanTime().getTime())/1000);
                scanInfo.setLatestCommit(commit);
                scanInfo.setScannedCommitCount(scanInfo.getScannedCommitCount()+1);
                repoDao.updateScanInfo(scanInfo);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void dealWithMethodCalls(JavaTree curJavaTree, CommonInfo commonInfo) {
        if (curJavaTree == null || curJavaTree.getMethodCallMap() == null || curJavaTree.getMethodCallMap().size() == 0) {
            return;
        }
        Map<String, List<MethodCall>> methodCallMap = curJavaTree.getMethodCallMap();
        List<MethodNode> methodNodes = curJavaTree.getMethodInfos();
        List<MethodCall> methodCalls = new ArrayList<>();
        //处理field add的方法调用
        for (FieldNode fieldNode : curJavaTree.getFieldInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(fieldNode.getChangeStatus()) && methodCallMap.keySet().contains(fieldNode.getUuid())) {
                List<MethodCall> list = methodCallMap.get(fieldNode.getUuid());
                for (MethodCall methodCall : list) {
                    String rawMethodUuid = findCalledMethod(methodCall, methodNodes);
                    if (rawMethodUuid != null) {
                        methodCall.setRawMethodUuid(rawMethodUuid);
                        methodCalls.add(methodCall);
                    }
                }
            }
        }

        //处理statement add的方法调用
        for (StatementNode statementNode : curJavaTree.getStatementInfos()) {
            if (BaseNode.ChangeStatus.ADD.equals(statementNode.getChangeStatus()) && methodCallMap.keySet().contains(statementNode.getUuid())) {
                List<MethodCall> list = methodCallMap.get(statementNode.getUuid());
                for (MethodCall methodCall : list) {
                    String rawMethodUuid = findCalledMethod(methodCall, methodNodes);
                    if (rawMethodUuid != null) {
                        methodCall.setRawMethodUuid(rawMethodUuid);
                        methodCalls.add(methodCall);
                    }
                }
            }
        }

        //fixme field,statement change情况新增调用识别待处理

        //save入库
        saveMethodCallList(methodCalls, commonInfo);
    }

    private void saveMethodCallList(List<MethodCall> methodCallList, CommonInfo commonInfo) {
        try {
            methodCallDao.insertMethodCallList(methodCallList, commonInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String findCalledMethod(MethodCall methodCall, List<MethodNode> methodNodes) {
        if (methodNodes == null) {
            return null;
        }
        for (MethodNode methodNode : methodNodes) {
            boolean isMatch = methodCall.getPackageName().equals(methodNode.getPackageName())
                    && methodCall.getClassName().equals(methodNode.getClassName())
                    && methodCall.getSignature().equals(methodNode.getSignature());
            if (isMatch && methodNode.isChangeCalledMethod()) {
                return methodNode.getUuid();
            }
        }
        return null;
    }

    /**
     * fixme 当field statement为change时，判断哪些调用是新增
     */
    private void dealWithChangeMethodCalls() {

    }

    private List<String> listFiles(File projectDir) {
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> (path.endsWith(".java") || path.endsWith(".cpp")),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        return pathList;
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


    private void scan (String repoUuid, String commitId, String branch, JGitHelper jGitHelper, String repoPath) {
        if (jGitHelper != null) {
            jGitHelper = new JGitHelper(repoPath);
        }
        //通过jgit拿到file列表
        jGitHelper.checkout(commitId);
        // key parentCommit value Map <key ADD CHANGE RENAME DELETE value >
        Map<String,Map<String, List<String>>> fileMap = jGitHelper.getFileList(commitId);

        //CLDiff输出路径
        String [] paths = repoPath.replace('\\','/').split("/");
        String outputPath = outputDir +  (IS_WINDOWS ?  "\\" : "/") + paths[paths.length -1] + (IS_WINDOWS ?  "\\" : "/") + commitId;
        //通过ClDiff拿到逻辑修改文件
        Map<String,Map<String,String>> logicalChangedFileMap = extractDiffFilePathFromClDiff(repoPath,commitId,outputPath);

        //merge情况直接跳过
        if (fileMap.keySet().size() > 1) {
            //JGitHelper jGitHelper, String commit, String outputPath, String repoUuid, String branch, Map<String,Map<String,String>> logicalChangedFileMap
            JSONObject result = mergeHandler.dealWithMerge(jGitHelper,commitId,outputPath,repoUuid,branch,logicalChangedFileMap,repoPath);
            if (result == null) {
                return;
            }
            RepoInfoTree preTree = (RepoInfoTree) result.get("pre");
            RepoInfoTree curTree = (RepoInfoTree) result.get("cur");
            CommonInfo commonInfo = (CommonInfo) result.get("commonInfo");

            JavaTree preJavaTree = (JavaTree) preTree.getRepoTree().get(Language.JAVA);
            JavaTree curJavaTree = (JavaTree) curTree.getRepoTree().get(Language.JAVA);
            //入库
            extractAndSaveInfo(preJavaTree,curJavaTree,commonInfo);
            return;
        }

        for (String preCommit : fileMap.keySet()) {
            CommonInfo preCommonInfo = constructCommonInfo(repoUuid,branch,preCommit,null,jGitHelper);
            CommonInfo curCommonInfo = constructCommonInfo(repoUuid,branch,commitId,preCommit,jGitHelper);
            Map<String, List<String>> map = fileMap.get(preCommit);

            //根据file列表构建preTree和curTree
            List<String> preRelatives = new ArrayList<>();
            List<String> curRelatives = new ArrayList<>();
            List<String> preFileList;
            List<String> curFileList;
            //抽取rename情况
            for (String str: map.get(RENAME)) {
                String[] renamePaths = str.split(":");
                preRelatives.add(renamePaths[0]);
                curRelatives.add(renamePaths[1]);
            }
            preRelatives.addAll(map.get(CHANGE));
            preRelatives.addAll(map.get(DELETE));
            preFileList = localizeFilePath(repoPath, preRelatives);
            curRelatives.addAll(map.get(CHANGE));
            curRelatives.addAll(map.get(ADD));
            curFileList = localizeFilePath(repoPath, curRelatives);

            //List<String> fileList, CommonInfo commonInfo, String repoUuid
            jGitHelper.checkout(preCommit);
            RepoInfoTree preRepoInfoTree = new RepoInfoTree(preFileList,preCommonInfo,repoUuid);
            jGitHelper.checkout(commitId);
            DependencyAnalysis.setRepoPathT(repoPath);
            RepoInfoTree curRepoInfoTree = new RepoInfoTree(curFileList,curCommonInfo,repoUuid);

            //Java语言mapping
            JavaTree preJavaTree = (JavaTree) preRepoInfoTree.getRepoTree().get(Language.JAVA);
            JavaTree curJavaTree = (JavaTree) curRepoInfoTree.getRepoTree().get(Language.JAVA);

            TrackerCore.mapping(preJavaTree,curJavaTree,preCommonInfo,repoUuid,branch,map,logicalChangedFileMap,outputPath,preCommit);
            //Java入库
            extractAndSaveInfo(preJavaTree,curJavaTree,curCommonInfo);

            //处理调用关系
            dealWithMethodCalls(curJavaTree, curCommonInfo);

            //rename情况入库，更新meta表
            if (map.get(RENAME).size() > 0) {
                updateRenameInfo(curJavaTree, curCommonInfo, map.get(RENAME));
            }
        }

    }

    /**
     * fixme 只抽取未改变的更新meta表，有改变的extractAndSaveInfo已入库
     * @param javaTree
     * @param commonInfo
     * @param renameList
     */
    private void updateRenameInfo(JavaTree javaTree, CommonInfo commonInfo, List<String> renameList) {
        Set<ClassNode> classNodeSet = new HashSet<>();
        Set<MethodNode> methodNodeSet = new HashSet<>();
        Set<FieldNode> fieldNodeSet = new HashSet<>();
        for (String str: renameList) {
            String[] renamePaths = str.split(":");
            String filePath = renamePaths[1];
            for (FileNode fileNode : javaTree.getFileInfos()) {
                if (filePath.equals(fileNode.getFilePath())) {
                    for (BaseNode baseNode : fileNode.getChildren()) {
                        ClassNode classNode = (ClassNode) baseNode;
                        if (BaseNode.ChangeStatus.UNCHANGED.equals(classNode.getChangeStatus())) {
                            classNodeSet.add(classNode);
                        }
                        //method
                        for (BaseNode mNode : classNode.getChildren()) {
                            if (BaseNode.ChangeStatus.UNCHANGED.equals(mNode.getChangeStatus())) {
                                methodNodeSet.add((MethodNode) mNode);
                            }
                        }
                        //field
                        for (BaseNode fNode : classNode.getFieldNodes()) {
                            if (BaseNode.ChangeStatus.UNCHANGED.equals(fNode.getChangeStatus())) {
                                fieldNodeSet.add((FieldNode) fNode);
                            }
                        }
                    }
                }
            }
        }
        saveMetaInfo(classNodeSet, methodNodeSet, fieldNodeSet, commonInfo);
    }

    private void saveMetaInfo(Set<ClassNode> classNodeSet, Set<MethodNode> methodNodeSet, Set<FieldNode> fieldNodeSet, CommonInfo commonInfo) {
        try {
            classDao.updateMetaInfo(classNodeSet, commonInfo);
            methodDao.updateMetaInfo(methodNodeSet, commonInfo);
            fieldDao.updateMetaInfo(fieldNodeSet, commonInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private CommonInfo constructCommonInfo(String repoUuid, String branch, String commit, String parentCommit, JGitHelper jGitHelper) {
        if (parentCommit == null || parentCommit.length() == 0) {
            parentCommit = commit;
        }
        jGitHelper.checkout(commit);

        Date commitDate = getDateByString(jGitHelper.getCommitTime(commit));
        String committer = jGitHelper.getAuthorName(commit);
        String commitMessage = jGitHelper.getMess(commit);
        // String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit
        return new CommonInfo(repoUuid, branch, commit, commitDate, committer, commitMessage, parentCommit);
    }


    private void extractAndSaveInfo(JavaTree preRepoInfo, JavaTree curRepoInfo, CommonInfo commonInfo) {
        //抽取需要入库的数据
        Map<String,Set<PackageNode>> packageMap = initNodeMap();
        Map<String,Set<FileNode>> fileMap = initNodeMap();
        Map<String,Set<ClassNode>> classMap = initNodeMap();
        Map<String,Set<MethodNode>> methodMap = initNodeMap();
        Map<String,Set<FieldNode>> fieldMap = initNodeMap();
        Map<String,Set<StatementNode>> statementMap = initNodeMap();

        if (curRepoInfo != null) {
            //curTree上搜索add change情况
            handleNode(packageMap, curRepoInfo.getPackageInfos());
            handleNode(fileMap, curRepoInfo.getFileInfos());
            handleNode(classMap, curRepoInfo.getClassInfos());
            handleNode(methodMap, curRepoInfo.getMethodInfos());
            handleNode(fieldMap, curRepoInfo.getFieldInfos());
            handleNode(statementMap, curRepoInfo.getStatementInfos());
        }

        if (preRepoInfo != null) {
            //preTree上搜索delete情况
            for (PackageNode packageNode: preRepoInfo.getPackageInfos()) {
                //packageNode没有删除情况，只有change
                if (!BaseNode.ChangeStatus.UNCHANGED.equals(packageNode.getChangeStatus())) {
                    packageMap.get("CHANGE").add(packageNode);
                }
            }
            handleNodeDelete(fileMap, preRepoInfo.getFileInfos());
            handleNodeDelete(classMap, preRepoInfo.getClassInfos());
            handleNodeDelete(methodMap, preRepoInfo.getMethodInfos());
            handleNodeDelete(fieldMap, preRepoInfo.getFieldInfos());
            handleNodeDelete(statementMap, preRepoInfo.getStatementInfos());
        }

        save(packageMap,fileMap,classMap,methodMap,fieldMap,statementMap,commonInfo);
    }

    @SuppressWarnings("unchecked")
    private <T> void handleNodeDelete(Map<String, Set<T>> map, List<? extends BaseNode> baseNodes) {
        baseNodes.stream().
                filter(t -> BaseNode.ChangeStatus.DELETE.equals(t.getChangeStatus())).
                forEach(t -> map.get("DELETE").add((T)t));
    }

    @SuppressWarnings("unchecked")
    private <T> void handleNode(Map<String, Set<T>> nodeMap, List<? extends BaseNode> nodeList) {
        for (BaseNode node : nodeList) {
//            if (BaseNode.ChangeStatus.CHANGE.equals(node.getChangeStatus()) && node.getVersion() == 1) {
//                node.setChangeStatus(BaseNode.ChangeStatus.ADD);
//            }
            T t = (T)node;
            if (BaseNode.ChangeStatus.ADD.equals(node.getChangeStatus())) {
                nodeMap.get("ADD").add(t);
            } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(node.getChangeStatus())) {
                nodeMap.get("CHANGE").add(t);
            }
        }
    }

    private <T>  Map<String,Set<T>> initNodeMap() {
        Map<String,Set<T>> result = new HashMap<>(4);
        result.put("ADD",new HashSet<>(128));
        result.put("CHANGE",new HashSet<>(128));
        result.put("DELETE",new HashSet<>(128));
        return result;
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


    /**
     * todo 放在 cldiff Adapter中 与 cldiff解耦
     */
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

    /**
     *将文件相对路径转成绝对路径
     */
    private List<String> localizeFilePath(String repoPath, List<String> filePath) {
        for (int i = 0; i < filePath.size() ; i++) {
            filePath.set(i, repoPath + "/" + filePath.get(i));
        }
        return filePath;
    }

    @SneakyThrows
    private void saveData(JavaTree repoInfo,CommonInfo commonInfo) {
        packageDao.insertPackageInfoList(repoInfo.getPackageInfos(),commonInfo);
        packageDao.insertRawPackageInfoList(repoInfo.getPackageInfos(),commonInfo);

        fileDao.insertFileInfoList(repoInfo.getFileInfos(),commonInfo);
        fileDao.insertRawFileInfoList(repoInfo.getFileInfos(),commonInfo);

        classDao.insertClassInfoList(repoInfo.getClassInfos(),commonInfo);
        classDao.insertRawClassInfoList(repoInfo.getClassInfos(),commonInfo);

        methodDao.insertMethodInfoList(repoInfo.getMethodInfos(),commonInfo);
        methodDao.insertRawMethodInfoList(repoInfo.getMethodInfos(),commonInfo);

        fieldDao.insertFieldInfoList(repoInfo.getFieldInfos(),commonInfo);
        fieldDao.insertRawFieldInfoList(repoInfo.getFieldInfos(),commonInfo);

        statementDao.insertStatementInfoList(repoInfo.getStatementInfos(),commonInfo);
        statementDao.insertRawStatementInfoList(repoInfo.getStatementInfos(),commonInfo);
        statementDao.insertStatementRelationList(repoInfo.getStatementInfos(),commonInfo);
    }


    /**
     * 返回最新扫描信息
     * @param repoId 代码仓库的 uuid
     * @return
     */
    @Override
    public ScanInfo getScanInfo(String repoId) {
        return repoDao.getScanInfo(repoId);
    }



    private String getRepoPathByUuid(String repoUuid) {
        if ("dubbo".equals(repoUuid)) {
            return "E:\\Lab\\scanProject\\dubbo";
        }

        if("iec-wepm-develop".equals(repoUuid)) {
            return IS_WINDOWS  ? "E:\\Lab\\iec-wepm-develop" :"/Users/tangyuan/Documents/Git/iec-wepm-develop";
        }

        if ("94eb2fd8-89de-11ea-801e-1b2730e40821".equals(repoUuid)) {
//            return "/home/fdse/codewisdom/repo/IssueTracker-Master";
//            return "E:\\Lab\\scanProject\\IssueTracker-Master";
            return "/Users/tangyuan/Documents/Git/IssueTracker-Master";
        }

        return "/Users/tangyuan/Documents/Git/IssueTracker-Master";
//        return "/home/fdse/codewisdom/repo/pom-manipulation-ext";
    }

    public static void main(String[] args) {
        Map<String,Map<String,String>> map = new ScanServiceImpl().extractDiffFilePathFromClDiff("/Users/tangyuan/Documents/Git/IssueTracker-Master","c2d014ec63e6079f62fe734f1f9650c0890e6569","/Users/tangyuan/Desktop/demo/IssueTracker-Master/c2d014ec63e6079f62fe734f1f9650c0890e6569");
        System.out.println(map);
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

    @Autowired
    public void setMethodCallDao(MethodCallDao methodCallDao) {
        this.methodCallDao = methodCallDao;
    }
}