/**
 * @description: 分析输出结果的meta data
 * @author: fancying
 * @create: 2019-09-25 20:23
 **/
package cn.edu.fudan.codetracker.handle;

import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.util.DirExplorer;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutputAnalysis {

    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    private String repoUuid;
    private String branch;
    private String outputDir;
    private String commitId;
    private JGitHelper jGitHelper;


    public OutputAnalysis(String repoUuid, String branch, String outputDir, JGitHelper jGitHelper) {
        this.repoUuid = repoUuid;
        this.branch = branch;
        this.outputDir = outputDir;
        this.jGitHelper = jGitHelper;
    }

    // entry point of analyzing relations about two commits
    public List<String> analyzeMetaInfo() {
        File file = outputDir.contains("\\") ?
                new File(outputDir + "\\" + commitId) :
                new File(outputDir + "/" + commitId);
        String metaPath = findMetaFile(file);
        JSONArray fileInfoJsonArray;
        JSONArray preCommits;
        try {
            String input = FileUtils.readFileToString(new File(metaPath), "UTF-8");
            JSONObject metaInfoJSON = JSONObject.parseObject(input);
            fileInfoJsonArray = metaInfoJSON.getJSONArray("files");
            JSONArray actions = metaInfoJSON.getJSONArray("actions");
            Map<JSONObject, String> diffFileAction = new HashMap<>();
            for (int i = 0; i < fileInfoJsonArray.size(); i++) {
                // only analyze java file
                if (fileInfoJsonArray.getJSONObject(i).getString("file_short_name").contains(".java")) {
                    diffFileAction.put(fileInfoJsonArray.getJSONObject(i), actions.getString(i));
                }
            }

            String pathPrefix = metaPath.replace("meta.json", "");
            // if preCommits include not only one record, then take into account merge situation
            preCommits = metaInfoJSON.getJSONArray("parents");

            String currFilePath;
            String prevFilePath;
            for (int i = 0; i < preCommits.size(); i++) {
                // construct change file list according to preCommit
                String preCommit = preCommits.getString(i);
                for (Map.Entry<JSONObject, String> m : diffFileAction.entrySet()) {
                    // three types of change relation handler ： ADD、DELETE、MODIFY
                    if (! m.getKey().getString("file_full_name").endsWith(".java")) {
                        continue;
                    }
                    ArrayList<String>  preFileList = new ArrayList<>();
                    ArrayList<String>  curFileList = new ArrayList<>();
                    List<String> addFilesList = new ArrayList<>();
                    List<String> deleteFilesList = new ArrayList<>();
                    List<String> diffPathList = new ArrayList<>();
                    if (m.getKey().getString("parent_commit").equals(preCommit)) {
                        if ("ADD".equals(m.getValue())) {
                            currFilePath =  pathPrefix + "/" + m.getKey().getString("curr_file_path");
                            addFilesList.add(isWindows ? pathUnixToWin(currFilePath) : currFilePath);
                        }
                        if ("DELETE".equals(m.getValue())) {
                            prevFilePath = pathPrefix + pathUnixToWin(m.getKey().getString("prev_file_path"));
                            deleteFilesList.add(isWindows ? pathUnixToWin(prevFilePath) : prevFilePath);
                        }
                        if ("MODIFY".equals(m.getValue())) {
                            prevFilePath = pathPrefix + "/" + m.getKey().getString("prev_file_path");
                            preFileList.add( isWindows ? pathUnixToWin(prevFilePath) : prevFilePath);
                            currFilePath = pathPrefix + "/" + m.getKey().getString("curr_file_path");
                            curFileList.add( isWindows ? pathUnixToWin(currFilePath) : currFilePath);
                            String diffPath = isWindows ? pathUnixToWin(currFilePath) : currFilePath;
                            diffPathList.add(diffPath);
                        }
                        // RepoInfoBuilder need to refactor for parentCommit is not null; so
                        RepoInfoBuilder preRepoInfo = new RepoInfoBuilder(repoUuid, preCommit, preFileList, jGitHelper, branch, null);
                        RepoInfoBuilder curRepoInfo = new RepoInfoBuilder(repoUuid, preCommit, curFileList, jGitHelper, branch, preCommit);
                        AnalyzeDiffFile analyzeDiffFile = new AnalyzeDiffFile(preRepoInfo, curRepoInfo);
                        analyzeDiffFile.addInfoConstruction(addFilesList);
                        analyzeDiffFile.deleteInfoConstruction(deleteFilesList);
                        //analyzeDiffFile.modifyInfoConstruction(preRepoInfo, curRepoInfo, diffPathList);
                        analyzeDiffFile.modifyInfoConstruction(preRepoInfo, preFileList, curRepoInfo, curFileList, diffPathList);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

 /*   private void analyzeMetaInfoFiles(Map<JSONObject, String> diffFileAction, AnalyzeDiffFile analyzeDiffFile, String preCommit, String metaPath) {
        String prevFilePath;
        String currFilePath;
        //Map<String, String> preCurrentFileMap = new HashMap<>();
        // 记录前后change 的文件列表
        List<String> prePathList = new ArrayList<>();
        List<String> curPathList = new ArrayList<>();
        List<String> diffPathList = new ArrayList<>();

        List<String> addFilesList = new ArrayList<>();
        List<String> deleteFilesList = new ArrayList<>();

        List<String> deletePath = new ArrayList<>();
        // 三种类型的文件处理
        for (Map.Entry<JSONObject, String> m : diffFileAction.entrySet()) {
            if (! m.getKey().getString("file_full_name").endsWith(".java")) {
                continue;
            }
            String parent_commit = m.getKey().getString("parent_commit");
            if (parent_commit.equals(preCommit)) {
                String diffPath = m.getKey().getString("diffPath");
                String prefix = metaPath.replace("meta.json", "");
                if ("MODIFY".equals(m.getValue())) {
                    prevFilePath = prefix + pathUnixToWin(m.getKey().getString("prev_file_path"));
                    currFilePath = prefix + pathUnixToWin(m.getKey().getString("curr_file_path"));
                    prePathList.add(prevFilePath);
                    curPathList.add(currFilePath);
                    diffPathList.add(pathUnixToWin(diffPath));
                }
                if ("ADD".equals(m.getValue())) {
                    currFilePath = prefix + pathUnixToWin(m.getKey().getString("curr_file_path"));
                    addFilesList.add(currFilePath);
                }
                if ("DELETE".equals(m.getValue())) {
                    prevFilePath = prefix + pathUnixToWin(m.getKey().getString("prev_file_path"));
                    deleteFilesList.add(prevFilePath);
                }
            }
        }




*//*        analyzeDiffFile.addInfoConstruction(addFilesList);
        analyzeDiffFile.deleteInfoConstruction(deleteFilesList);
        analyzeDiffFile.modifyInfoConstruction(prePathList, curPathList, diffPathList);

        analyzeDiffFile.packageRelationAnalyze(notChangedFileList);*//*
    }
*/
    private String deletePrefix(String curPathList, String preCommit) {
        String p = curPathList.replace(preCommit, "--------");

        String s[] = p.split("--------");
        System.out.println(s);
        return pathUnixToWin(s[1]);
    }

    private String pathUnixToWin(String path) {
        return path.replace("/", "\\");
    }

    private String findMetaFile(File projectDir) {
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith("meta.json"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        return pathList.get(0);
    }

    private List<String> listJavaFiles(File projectDir) {
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith(".java"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        return pathList;
    }


}