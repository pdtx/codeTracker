///**
// * @description: 分析输出结果的meta data
// * @author: fancying
// * @create: 2019-09-25 20:23
// **/
//package cn.edu.fudan.codetracker.core;
//
//import cn.edu.fudan.codetracker.dao.*;
//import cn.edu.fudan.codetracker.jgit.JGitHelper;
//import cn.edu.fudan.codetracker.util.DirExplorer;
//import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FileUtils;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//public class MetaInfoAnalysis {
//
//    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
//
//    private String repoUuid;
//    private String branch;
//    private String outputDir;
//    private String commitId;
//    private JGitHelper jGitHelper;
//    private int changeImportCount;
//    private List<String> preCommitIds;
//    private int mergeNum;
//    /**
//     * key commit; value list
//     */
//    private Map<String, List<String>>  preFileListMap;
//    private Map<String, List<String>>  curFileListMap;
//    private Map<String, List<String>> addFilesListMap;
//    private Map<String, List<String>> deleteFilesListMap;
//
//
//    public MetaInfoAnalysis(String repoUuid, String branch, String outputDir, JGitHelper jGitHelper, String commitId) {
//        this.repoUuid = repoUuid;
//        this.branch = branch;
//        this.outputDir = outputDir;
//        this.jGitHelper = jGitHelper;
//        this.commitId = commitId;
//        init();
//    }
//
//    private void init() {
//        preFileListMap = new HashMap<>(2);
//        curFileListMap = new HashMap<>(2);
//        addFilesListMap = new HashMap<>(2);
//        deleteFilesListMap = new HashMap<>(2);
//        preCommitIds = new ArrayList<>();
//    }
//
//    /**
//     * entry point of analyzing relations about two commits
//     */
//    public List<AnalyzeDiffFile> analyzeMetaInfo(ProxyDao proxyDao) {
//        List<AnalyzeDiffFile> analyzeDiffFiles = new ArrayList<>(2);
//        File file = outputDir.contains("\\") ?
//                new File(outputDir + "\\" + commitId) :
//                new File(outputDir + "/" + commitId);
//        String metaPath = findMetaFile(file);
//        if (metaPath == null || metaPath.length() == 0) {
//            return analyzeDiffFiles;
//        }
//        JSONArray fileInfoJsonArray;
//        JSONArray preCommits;
//        String input ;
//        try {
//            input = FileUtils.readFileToString(new File(metaPath), "UTF-8");
//        } catch (IOException e) {
//            log.error("meta file path can not find:{}", metaPath);
//            return analyzeDiffFiles;
//        }
//
//        JSONObject metaInfoJson = JSONObject.parseObject(input);
//        fileInfoJsonArray = metaInfoJson.getJSONArray("files");
//        JSONArray actions = metaInfoJson.getJSONArray("actions");
//        Map<JSONObject, String> diffFileAction = new HashMap<>(16);
//        for (int i = 0; i < fileInfoJsonArray.size(); i++) {
//            // only analyze java file
//            if (fileInfoJsonArray.getJSONObject(i).getString("file_short_name").contains(".java")) {
//                diffFileAction.put(fileInfoJsonArray.getJSONObject(i), actions.getString(i));
//            }
//        }
//
//        String pathPrefix = metaPath.replace("meta.json", "");
//        // if preCommits include not only one record, then take into account merge situation
//        preCommits = metaInfoJson.getJSONArray("parents");
//        for (int i = 0; i < preCommits.size(); i++) {
//            // construct change file list according to preCommit
//            String preCommit = preCommits.getString(i);
//            List<String> diffPathList = new ArrayList<>();
//            List<String> fileNameList = new ArrayList<>();
//            List<String> addFileNameList = new ArrayList<>();
//            List<String> deleteFileNameList = new ArrayList<>();
//            addFilesListMap.put(preCommit, new ArrayList<>());
//            deleteFilesListMap.put(preCommit, new ArrayList<>());
//            preFileListMap.put(preCommit, new ArrayList<>());
//            curFileListMap.put(preCommit, new ArrayList<>());
//            for (Map.Entry<JSONObject, String> m : diffFileAction.entrySet()) {
//                // three types of change relation handler ： ADD、DELETE、MODIFY
//                if (m.getKey().getString("parent_commit").equals(preCommit) &&
//                        m.getKey().getString("file_short_name").endsWith(".java")) {
//                    // ignore test class and enum class
//                    String shortName = m.getKey().getString("file_short_name").toLowerCase();
//                    String fullName = m.getKey().getString("file_full_name").toLowerCase();
//                    if (fullName.contains("/test/") ||
//                            shortName.endsWith("test.java") ||
//                            shortName.endsWith("tests.java") ||
//                            shortName.startsWith("test") ||
//                            shortName.endsWith("enum.java")) {
//                        continue;
//                    }
//                    if ("ADD".equals(m.getValue())) {
//                        try {
//                            String path;
//                            if (m.getKey().containsKey("curr_file_path")) {
//                                path = m.getKey().getString("curr_file_path") ;
//                            }else {
//                                path = m.getKey().getString("prev_file_path") ;
//                                log.error("ADD situation: curr_file_path lack,use prev_file_path" + metaPath);
//                            }
//                            String currFilePath =  pathPrefix + "/" + path;
//                            addFilesListMap.get(preCommit).add(IS_WINDOWS ? pathUnixToWin(currFilePath) : currFilePath);
//                            addFileNameList.add(m.getKey().getString("file_full_name"));
//                        }catch (NullPointerException e) {
//                            log.error("ADD situation: curr_file_path and prev_file_path lack" + metaPath);
//                        }
//                        continue;
//                    }
//                    if ("DELETE".equals(m.getValue())) {
//                        try {
//                            String path;
//                            if (m.getKey().containsKey("prev_file_path")) {
//                                path = m.getKey().getString("prev_file_path") ;
//                            }else {
//                                path = m.getKey().getString("curr_file_path") ;
//                                log.error("DELETE situation: prev_file_path lack,use curr_file_path" + metaPath);
//                            }
//                            String prevFilePath = pathPrefix + "/" +   path;
//                            deleteFilesListMap.get(preCommit).add(IS_WINDOWS ? pathUnixToWin(prevFilePath) : prevFilePath);
//                            deleteFileNameList.add(m.getKey().getString("file_full_name"));
//                        }catch (NullPointerException e) {
//                            log.error("DELETE situation: curr_file_path and prev_file_path lack" + metaPath);
//                        }
//                        continue;
//                    }
//                    if ("MODIFY".equals(m.getValue())) {
//                        if (m.getKey().containsKey("diffPath")) {
//                            String diffPath = pathPrefix + m.getKey().getString("diffPath");
//                            fileNameList.add(m.getKey().getString("file_full_name"));
//                            diffPathList.add(IS_WINDOWS ? pathUnixToWin(diffPath) : diffPath);
//                            String prevFilePath = pathPrefix + m.getKey().getString("prev_file_path");
//                            preFileListMap.get(preCommit).add( IS_WINDOWS ? pathUnixToWin(prevFilePath) : prevFilePath);
//                            String currFilePath = pathPrefix + m.getKey().getString("curr_file_path");
//                            curFileListMap.get(preCommit).add( IS_WINDOWS ? pathUnixToWin(currFilePath) : currFilePath);
//                        } else {
//                            log.error("CHANGE situation: diffPath lack! " + metaPath + " id :" +  m.getKey().getString("id"));
//                        }
//                    }
//                }
//            }
//            // RepoInfoBuilder need to refactor for parentCommit is not null; so
//            RepoInfoBuilder preRepoInfo = new RepoInfoBuilder(repoUuid, preCommit, preFileListMap.get(preCommit), jGitHelper, branch, null, fileNameList);
//            RepoInfoBuilder curRepoInfo = new RepoInfoBuilder(repoUuid, commitId, curFileListMap.get(preCommit), jGitHelper, branch, preCommit, fileNameList);
//            RepoInfoBuilder addRepoInfo = new RepoInfoBuilder(curRepoInfo, addFilesListMap.get(preCommit), true, addFileNameList);
//            RepoInfoBuilder deleteRepoInfo = new RepoInfoBuilder(curRepoInfo, deleteFilesListMap.get(preCommit), false, deleteFileNameList);
//            AnalyzeDiffFile analyzeDiffFile = new AnalyzeDiffFile(proxyDao, preRepoInfo, curRepoInfo);
//            analyzeDiffFile.addInfoConstruction(addRepoInfo);
//            analyzeDiffFile.deleteInfoConstruction(deleteRepoInfo);
//            analyzeDiffFile.modifyInfoConstruction(fileNameList, diffPathList);
//            analyzeDiffFiles.add(analyzeDiffFile);
//            changeImportCount = addRepoInfo.getImportCount() - deleteRepoInfo.getImportCount() - preRepoInfo.getImportCount() + curRepoInfo.getImportCount();
//            preCommitIds.add(preCommit);
//        }
//        mergeNum = jGitHelper.mergeJudgment(commitId) ;
//        return analyzeDiffFiles;
//    }
//
//    private String pathUnixToWin(String path) {
//        return path.replace("/", "\\");
//    }
//
//    private String findMetaFile(File projectDir) {
//        List<String> pathList = new ArrayList<>();
//        new DirExplorer((level, path, file) -> path.endsWith("meta.json"),
//                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
//        if (pathList.size() == 0) {
//            return null;
//        }
//        return pathList.get(0);
//    }
//
//    private List<String> listJavaFiles(File projectDir) {
//        List<String> pathList = new ArrayList<>();
//        new DirExplorer((level, path, file) -> path.endsWith(".java"),
//                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
//        return pathList;
//    }
//
//    public int getChangeImportCount() { return changeImportCount; }
//
//    public List<String> getPreCommitIds() { return preCommitIds; }
//
//
//    public Map<String, List<String>> getPreFileListMap() {
//        return preFileListMap;
//    }
//
//    public Map<String, List<String>> getCurFileListMap() {
//        return curFileListMap;
//    }
//
//    public Map<String, List<String>> getAddFilesListMap() {
//        return addFilesListMap;
//    }
//
//    public Map<String, List<String>> getDeleteFilesListMap() {
//        return deleteFilesListMap;
//    }
//
//    public int getMergeNum() {
//        return mergeNum;
//    }
//}