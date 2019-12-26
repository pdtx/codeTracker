/**
 * @description: 分析输出结果的meta data
 * @author: fancying
 * @create: 2019-09-25 20:23
 **/
package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.util.DirExplorer;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MetaInfoAnalysis {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private String repoUuid;
    private String branch;
    private String outputDir;
    private String commitId;
    private JGitHelper jGitHelper;
    private int changeImportCount;
    private String preCommitId;
    private ArrayList<String>  preFileList;
    private ArrayList<String>  curFileList;
    private List<String> addFilesList;
    private List<String> deleteFilesList;


    public MetaInfoAnalysis(String repoUuid, String branch, String outputDir, JGitHelper jGitHelper, String commitId) {
        this.repoUuid = repoUuid;
        this.branch = branch;
        this.outputDir = outputDir;
        this.jGitHelper = jGitHelper;
        this.commitId = commitId;
    }

    /**
     * entry point of analyzing relations about two commits
     */
    public List<AnalyzeDiffFile> analyzeMetaInfo(ProxyDao proxyDao) {
        List<AnalyzeDiffFile> analyzeDiffFiles = new ArrayList<>(2);
        File file = outputDir.contains("\\") ?
                new File(outputDir + "\\" + commitId) :
                new File(outputDir + "/" + commitId);
        String metaPath = findMetaFile(file);
        if (metaPath == null || metaPath.length() == 0) {
            return analyzeDiffFiles;
        }
        JSONArray fileInfoJsonArray;
        JSONArray preCommits;
        try {
            String input = FileUtils.readFileToString(new File(metaPath), "UTF-8");
            JSONObject metaInfoJson = JSONObject.parseObject(input);
            fileInfoJsonArray = metaInfoJson.getJSONArray("files");
            JSONArray actions = metaInfoJson.getJSONArray("actions");
            Map<JSONObject, String> diffFileAction = new HashMap<>();
            for (int i = 0; i < fileInfoJsonArray.size(); i++) {
                // only analyze java file
                if (fileInfoJsonArray.getJSONObject(i).getString("file_short_name").contains(".java")) {
                    diffFileAction.put(fileInfoJsonArray.getJSONObject(i), actions.getString(i));
                }
            }

            String pathPrefix = metaPath.replace("meta.json", "");
            // if preCommits include not only one record, then take into account merge situation
            preCommits = metaInfoJson.getJSONArray("parents");

            String currFilePath;
            String prevFilePath;
            preFileList = new ArrayList<>();
            curFileList = new ArrayList<>();
            addFilesList = new ArrayList<>();
            deleteFilesList = new ArrayList<>();
            List<String> diffPathList = new ArrayList<>();
            List<String> fileNameList = new ArrayList<>();
            List<String> addFileNameList = new ArrayList<>();
            List<String> deleteFileNameList = new ArrayList<>();
            String preCommit ;
            for (int i = 0; i < preCommits.size(); i++) {
                // construct change file list according to preCommit
                preCommit = preCommits.getString(i);
                for (Map.Entry<JSONObject, String> m : diffFileAction.entrySet()) {
                    // three types of change relation handler ： ADD、DELETE、MODIFY
                    if (m.getKey().getString("parent_commit").equals(preCommit) &&
                            m.getKey().getString("file_short_name").endsWith(".java")) {
                        // ignore test class and enum class
                        if (m.getKey().getString("file_short_name").toLowerCase().endsWith("test.java") ||
                                m.getKey().getString("file_short_name").toLowerCase().endsWith("tests.java") ||
                                m.getKey().getString("file_short_name").toLowerCase().startsWith("test") ||
                                m.getKey().getString("file_short_name").toLowerCase().endsWith("enum.java")) {
                            continue;
                        }
                        if ("ADD".equals(m.getValue())) {
                            try {
                                String path;
                                if (m.getKey().containsKey("curr_file_path")) {
                                    path = m.getKey().getString("curr_file_path") ;
                                }else {
                                    path = m.getKey().getString("prev_file_path") ;
                                    log.error("ADD situation: curr_file_path lack,use prev_file_path" + metaPath);
                                }
                                currFilePath =  pathPrefix + "/" + path;
                                addFilesList.add(IS_WINDOWS ? pathUnixToWin(currFilePath) : currFilePath);
                                addFileNameList.add(m.getKey().getString("file_full_name"));
                            }catch (NullPointerException e) {
                                log.error("ADD situation: curr_file_path and prev_file_path lack" + metaPath);
                            }
                            continue;
                        }
                        if ("DELETE".equals(m.getValue())) {
                            try {
                                String path;
                                if (m.getKey().containsKey("prev_file_path")) {
                                    path = m.getKey().getString("prev_file_path") ;
                                }else {
                                    path = m.getKey().getString("curr_file_path") ;
                                    log.error("DELETE situation: prev_file_path lack,use curr_file_path" + metaPath);
                                }
                                prevFilePath = pathPrefix + "/" +   path;
                                deleteFilesList.add(IS_WINDOWS ? pathUnixToWin(prevFilePath) : prevFilePath);
                                deleteFileNameList.add(m.getKey().getString("file_full_name"));
                            }catch (NullPointerException e) {
                                log.error("DELETE situation: curr_file_path and prev_file_path lack" + metaPath);
                            }
                            continue;
                        }
                        if ("MODIFY".equals(m.getValue())) {
                            if (m.getKey().containsKey("diffPath")) {
                                String diffPath = pathPrefix + m.getKey().getString("diffPath");
                                fileNameList.add(m.getKey().getString("file_full_name"));
                                diffPathList.add(IS_WINDOWS ? pathUnixToWin(diffPath) : diffPath);
                                prevFilePath = pathPrefix + m.getKey().getString("prev_file_path");
                                preFileList.add( IS_WINDOWS ? pathUnixToWin(prevFilePath) : prevFilePath);
                                currFilePath = pathPrefix + m.getKey().getString("curr_file_path");
                                curFileList.add( IS_WINDOWS ? pathUnixToWin(currFilePath) : currFilePath);
                            } else {
                                log.error("CHANGE situation: diffPath lack! " + metaPath + " id :" +  m.getKey().getString("id"));
                            }
                        }
                    }
                }
                // RepoInfoBuilder need to refactor for parentCommit is not null; so
                RepoInfoBuilder preRepoInfo = new RepoInfoBuilder(repoUuid, preCommit, preFileList, jGitHelper, branch, null, fileNameList);
                RepoInfoBuilder curRepoInfo = new RepoInfoBuilder(repoUuid, commitId, curFileList, jGitHelper, branch, preCommit, fileNameList);
                AnalyzeDiffFile analyzeDiffFile = new AnalyzeDiffFile(proxyDao, preRepoInfo, curRepoInfo);
                RepoInfoBuilder addRepoInfo = new RepoInfoBuilder(curRepoInfo, addFilesList, true, addFileNameList);
                analyzeDiffFile.addInfoConstruction(addRepoInfo);
                RepoInfoBuilder deleteRepoInfo = new RepoInfoBuilder(curRepoInfo, deleteFilesList, false, deleteFileNameList);
                analyzeDiffFile.deleteInfoConstruction(deleteRepoInfo);
                analyzeDiffFile.modifyInfoConstruction(fileNameList, diffPathList);
                analyzeDiffFiles.add(analyzeDiffFile);
                changeImportCount = addRepoInfo.getImportCount() - deleteRepoInfo.getImportCount() - preRepoInfo.getImportCount() + curRepoInfo.getImportCount();
                preCommitId = preCommit;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return analyzeDiffFiles;
    }

    private String pathUnixToWin(String path) {
        return path.replace("/", "\\");
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

    private List<String> listJavaFiles(File projectDir) {
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith(".java"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        return pathList;
    }

    public int getChangeImportCount() { return changeImportCount; }

    public String getPreCommitId() { return preCommitId; }

    public ArrayList<String> getPreFileList() { return preFileList; }

    public ArrayList<String> getCurFileList() { return curFileList; }

    public List<String> getAddFilesList() { return addFilesList; }

    public List<String> getDeleteFilesList() { return deleteFilesList; }
}