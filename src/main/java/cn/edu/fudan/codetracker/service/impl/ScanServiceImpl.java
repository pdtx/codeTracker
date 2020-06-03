/**
 * @description:
 * @author: fancying
 * @create: 2019-09-25 20:41
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.constants.ScanStatus;
import cn.edu.fudan.codetracker.core.*;
import cn.edu.fudan.codetracker.core.tree.JavaTree;
import cn.edu.fudan.codetracker.core.tree.Language;
import cn.edu.fudan.codetracker.core.tree.RepoInfoTree;
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
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ScanServiceImpl implements ScanService {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static ThreadLocal<String> repoPath = new ThreadLocal<>();

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
//        repoPath.set(restInterface.getCodeServiceRepo(repoUuid));
        repoPath.set(getRepoPathByUuid(repoUuid));
//        String repoPath = restInterface.getCodeServiceRepo(repoUuid);
//        String repoPath = getRepoPathByUuid(repoUuid);
        JGitHelper jGitHelper = new JGitHelper(repoPath.get());
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, beginCommit, false);
        log.info("commit size : " +  commitList.size());
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath.get(), jGitHelper, commitList, false);
        repoDao.updateScanStatus(repoUuid, branch, isAbort ? ScanStatus.ABORTED : ScanStatus.SCANNED);
//        restInterface.freeRepo(repoUuid, repoPath.get());
    }

    @Async("taskExecutor")
    @Override
    public void autoUpdate(String repoUuid, String branch) {
        String commitId = findScanLatest(repoUuid, branch);
        if (commitId == null) {
            log.warn("Update Scan Error: this repo [{}] hasn't been scanned!", repoUuid);
            return;
        }

        repoDao.updateScanStatus(repoUuid, branch, ScanStatus.SCANNING);
//        repoPath.set(restInterface.getCodeServiceRepo(repoUuid));
        repoPath.set(getRepoPathByUuid(repoUuid));
        JGitHelper jGitHelper = new JGitHelper(repoPath.get());
        List<String> commitList = jGitHelper.getCommitListByBranchAndBeginCommit(branch, commitId, true);
        log.info("commit size : " +  commitList.size());
        boolean isAbort = scanCommitList(repoUuid, branch, repoPath.get(), jGitHelper, commitList, true);
        repoDao.updateScanStatus(repoUuid, branch, isAbort ? ScanStatus.ABORTED : ScanStatus.SCANNED);
//        restInterface.freeRepo(repoUuid, repoPath.get());
    }

    /**
     * FIXME need @Transactional
     */
    private boolean scanCommitList(String repoUuid, String branch, String repoPath, JGitHelper jGitHelper, List<String> commitList, boolean isInit) {
        int num = 0;
        try {
            for (String commit : commitList) {
                log.info("start commit：{} {}" , ++num, commit);
                if (isInit) {
                    scan(repoUuid , commit, branch, jGitHelper, repoPath);
                } else {
                    jGitHelper.checkout(branch);
                    jGitHelper.checkout(commit);
                    CommonInfo commonInfo = constructCommonInfo(repoUuid,branch,commit,null,jGitHelper);
                    File file = new File(repoPath);
                    //List<String> fileList, CommonInfo commonInfo, String repoUuid
                    RepoInfoTree repoInfoTree = new RepoInfoTree(listFiles(file),commonInfo,repoUuid);
                    JavaTree javaTree = (JavaTree) repoInfoTree.getRepoTree().get(Language.JAVA);
                    travelAndSetChangeRelation(javaTree.getPackageInfos());
                    saveData(javaTree,commonInfo);
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
        jGitHelper.checkout(branch);
        jGitHelper.checkout(commitId);
        Map<String,Map<String, List<String>>> fileMap = jGitHelper.getFileList(commitId);

        //CLDiff输出路径
        String [] paths = repoPath.replace('\\','/').split("/");
        String outputPath = outputDir +  (IS_WINDOWS ?  "\\" : "/") + paths[paths.length -1] + (IS_WINDOWS ?  "\\" : "/") + commitId;

        //merge情况直接跳过
        if (fileMap.keySet().size() > 1) {
            dealWithMerge(jGitHelper,commitId,repoPath,outputPath);
            return;
        }

        for (String s : fileMap.keySet()) {
            String preCommit = s;
            CommonInfo preCommonInfo = constructCommonInfo(repoUuid,branch,preCommit,null,jGitHelper);
            CommonInfo curCommonInfo = constructCommonInfo(repoUuid,branch,commitId,preCommit,jGitHelper);
            Map<String, List<String>> map = fileMap.get(preCommit);

            //根据file列表构建preTree和curTree
            List<String> preRelatives = new ArrayList<>();
            List<String> curRelatives = new ArrayList<>();
            List<String> preFileList;
            List<String> curFileList;
            //抽取rename情况
            for (String str: map.get("RENAME")) {
                String[] renamePaths = str.split(":");
                preRelatives.add(renamePaths[0]);
                curRelatives.add(renamePaths[1]);
            }
            preRelatives.addAll(map.get("CHANGE"));
            preRelatives.addAll(map.get("DELETE"));
            preFileList = localizeFilePath(repoPath, preRelatives);
            curRelatives.addAll(map.get("CHANGE"));
            curRelatives.addAll(map.get("ADD"));
            curFileList = localizeFilePath(repoPath, curRelatives);

            //List<String> fileList, CommonInfo commonInfo, String repoUuid
            jGitHelper.checkout(preCommit);
            RepoInfoTree preRepoInfoTree = new RepoInfoTree(preFileList,preCommonInfo,repoUuid);
            jGitHelper.checkout(commitId);
            RepoInfoTree curRepoInfoTree = new RepoInfoTree(curFileList,curCommonInfo,repoUuid);

            //Java语言mapping
            JavaTree preJavaTree = (JavaTree) preRepoInfoTree.getRepoTree().get(Language.JAVA);
            JavaTree curJavaTree = (JavaTree) curRepoInfoTree.getRepoTree().get(Language.JAVA);
            //通过ClDiff拿到逻辑修改文件
            Map<String,Map<String,String>> logicalChangedFileMap = extractDiffFilePathFromClDiff(repoPath,commitId,outputPath);

            TrackerCore.mapping(preJavaTree,curJavaTree,preCommonInfo,repoUuid,branch,map,logicalChangedFileMap,outputPath,preCommit);
            //Java入库
            extractAndSaveInfo(preJavaTree,curJavaTree,curCommonInfo);


        }

    }

    private void dealWithMerge(JGitHelper jGitHelper, String commit, String repoPath, String outputPath) {
        Map<String, List<DiffEntry>> conflictInfo = jGitHelper.getConflictDiffEntryList(commit);
        if (conflictInfo == null || conflictInfo.size() == 0) {
            return;
        }
        String str = null;
        List<DiffEntry> list = null;
        for (Map.Entry<String,List<DiffEntry>> entry: conflictInfo.entrySet()) {
            str = entry.getKey();
            list = entry.getValue();
        }
        if (str == null || list == null || list.size() == 0) {
            return;
        }

        //获取parent1，parent2；parent1和parent2是通过比对提交者和时间确定的顺序
        String[] parents = str.split("|");
        String parent1 = parents[0];
        String parent2 = parents[1];

        //目前策略：处理conflict，首先由parent1与current进行对比，若有add情况，去parent2中寻找有无对应节点
        Map<String,Map<String,String>> logicalChangedFileMap = extractDiffFilePathFromClDiff(repoPath,commit,outputPath);
        //构造三棵树
        List<String> fileList = new ArrayList<>();


    }

    private CommonInfo constructCommonInfo(String repoUuid, String branch, String commit, String parentCommit, JGitHelper jGitHelper) {
        if (parentCommit == null || parentCommit.length() == 0) {
            parentCommit = commit;
        }
        jGitHelper.checkout(commit);
        try{
            Date commitDate = FORMATTER.parse(jGitHelper.getCommitTime(commit));
            String committer = jGitHelper.getAuthorName(commit);
            String commitMessage = jGitHelper.getMess(commit);
            // String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit
            CommonInfo commonInfo = new CommonInfo(repoUuid, branch, commit, commitDate, committer, commitMessage, parentCommit);
            return commonInfo;
        }catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void extractAndSaveInfo(JavaTree preRepoInfo, JavaTree curRepoInfo, CommonInfo commonInfo) {
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

        if (curRepoInfo != null) {
            //curTree上搜索add change情况
            for (PackageNode packageNode : curRepoInfo.getPackageInfos()) {
                if (BaseNode.ChangeStatus.CHANGE.equals(packageNode.getChangeStatus()) && packageNode.getVersion() == 1) {
                    packageNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                }
                if (BaseNode.ChangeStatus.ADD.equals(packageNode.getChangeStatus())) {
                    packageMap.get("ADD").add(packageNode);
                } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(packageNode.getChangeStatus())) {
                    packageMap.get("CHANGE").add(packageNode);
                }
            }
            for (FileNode fileNode : curRepoInfo.getFileInfos()) {
                if (BaseNode.ChangeStatus.CHANGE.equals(fileNode.getChangeStatus()) && fileNode.getVersion() == 1) {
                    fileNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                }
                if (BaseNode.ChangeStatus.ADD.equals(fileNode.getChangeStatus())) {
                    fileMap.get("ADD").add(fileNode);
                } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(fileNode.getChangeStatus())) {
                    fileMap.get("CHANGE").add(fileNode);
                }
            }
            for (ClassNode classNode : curRepoInfo.getClassInfos()) {
                if (BaseNode.ChangeStatus.CHANGE.equals(classNode.getChangeStatus()) && classNode.getVersion() == 1) {
                    classNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                }
                if (BaseNode.ChangeStatus.ADD.equals(classNode.getChangeStatus())) {
                    classMap.get("ADD").add(classNode);
                } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(classNode.getChangeStatus())) {
                    classMap.get("CHANGE").add(classNode);
                }
            }
            for (MethodNode methodNode : curRepoInfo.getMethodInfos()) {
                if (BaseNode.ChangeStatus.CHANGE.equals(methodNode.getChangeStatus()) && methodNode.getVersion() == 1) {
                    methodNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                }
                if (BaseNode.ChangeStatus.ADD.equals(methodNode.getChangeStatus())) {
                    methodMap.get("ADD").add(methodNode);
                } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(methodNode.getChangeStatus())) {
                    methodMap.get("CHANGE").add(methodNode);
                }
            }
            for (FieldNode fieldNode : curRepoInfo.getFieldInfos()) {
                if (BaseNode.ChangeStatus.CHANGE.equals(fieldNode.getChangeStatus()) && fieldNode.getVersion() == 1) {
                    fieldNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                }
                if (BaseNode.ChangeStatus.ADD.equals(fieldNode.getChangeStatus())) {
                    fieldMap.get("ADD").add(fieldNode);
                } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(fieldNode.getChangeStatus())) {
                    fieldMap.get("CHANGE").add(fieldNode);
                }
            }
            for (StatementNode statementNode : curRepoInfo.getStatementInfos()) {
                if (BaseNode.ChangeStatus.CHANGE.equals(statementNode.getChangeStatus()) && statementNode.getVersion() == 1) {
                    statementNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                }
                if (BaseNode.ChangeStatus.ADD.equals(statementNode.getChangeStatus())) {
                    statementMap.get("ADD").add(statementNode);
                } else if (!BaseNode.ChangeStatus.UNCHANGED.equals(statementNode.getChangeStatus())) {
                    statementMap.get("CHANGE").add(statementNode);
                }
            }
        }

        if (preRepoInfo != null) {
            //preTree上搜索delete情况
            for (PackageNode packageNode: preRepoInfo.getPackageInfos()) {
                //packageNode没有删除情况，只有change
                if (!BaseNode.ChangeStatus.UNCHANGED.equals(packageNode.getChangeStatus())) {
                    packageMap.get("CHANGE").add(packageNode);
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
        }

        save(packageMap,fileMap,classMap,methodMap,fieldMap,statementMap,commonInfo);
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

    private void saveData(JavaTree repoInfo,CommonInfo commonInfo) {
        try {

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

        }catch (Exception e) {
            e.printStackTrace();
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



    private String getRepoPathByUuid(String repoUuid) {
        if ("dubbo".equals(repoUuid)) {
            return "E:\\Lab\\project\\dubbo";
        }

        if("iec-wepm-develop".equals(repoUuid)) {
            return IS_WINDOWS  ? "E:\\Lab\\iec-wepm-develop" :"/Users/tangyuan/Documents/Git/iec-wepm-develop";
        }

        if ("94eb2fd8-89de-11ea-801e-1b2730e40821".equals(repoUuid)) {
            return "/home/fdse/codewisdom/repo/IssueTracker-Master";
//            return "E:\\Lab\\scanProject\\IssueTracker-Master";
//            return "/Users/tangyuan/Documents/Git/IssueTracker-Master";
        }

        return "/home/fdse/codewisdom/repo/pom-manipulation-ext";
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


}