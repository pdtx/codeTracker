/**
 * @description: 分析输出结果的meta data
 * @author: fancying
 * @create: 2019-09-25 20:23
 **/
package cn.edu.fudan.codetracker.handler;

import cn.edu.fudan.codetracker.dao.*;
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


    public OutputAnalysis(String repoUuid, String branch, String outputDir, JGitHelper jGitHelper, String commitId) {
        this.repoUuid = repoUuid;
        this.branch = branch;
        this.outputDir = outputDir;
        this.jGitHelper = jGitHelper;
        this.commitId = commitId;
    }

    // entry point of analyzing relations about two commits
    public List<AnalyzeDiffFile> analyzeMetaInfo(PackageDao packageDao, FileDao fileDao, ClassDao classDao, FieldDao fieldDao, MethodDao methodDao) {
        List<AnalyzeDiffFile> analyzeDiffFiles = new ArrayList<>(2);
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
            ArrayList<String>  preFileList = new ArrayList<>();
            ArrayList<String>  curFileList = new ArrayList<>();
            List<String> addFilesList = new ArrayList<>();
            List<String> deleteFilesList = new ArrayList<>();
            List<String> diffPathList = new ArrayList<>();
            List<String> fileNameList = new ArrayList<>();
            String preCommit ;
            for (int i = 0; i < preCommits.size(); i++) {
                // construct change file list according to preCommit
                preCommit = preCommits.getString(i);
                for (Map.Entry<JSONObject, String> m : diffFileAction.entrySet()) {
                    // three types of change relation handler ： ADD、DELETE、MODIFY
                    if (m.getKey().getString("parent_commit").equals(preCommit) &&
                            m.getKey().getString("file_full_name").endsWith(".java")) {
                        if ("ADD".equals(m.getValue())) {
                            try {
                                String path;
                                if (m.getKey().containsKey("curr_file_path")) {
                                    path = m.getKey().getString("curr_file_path") ;
                                }else {
                                    path = m.getKey().getString("prev_file_path") ;
                                    System.out.println("==========ADD1====================");
                                    System.out.println(metaPath);
                                    System.out.println("===========ADD1===================");
                                }
                                currFilePath =  pathPrefix + "/" + path;
                                addFilesList.add(isWindows ? pathUnixToWin(currFilePath) : currFilePath);
                            }catch (NullPointerException e) {
                                System.out.println("==========ADD2====================");
                                System.out.println(metaPath);
                                System.out.println("===========ADD2===================");
                            }

                        }
                        if ("DELETE".equals(m.getValue())) {
                            try {
                                String path;
                                if (m.getKey().containsKey("prev_file_path")) {
                                    path = m.getKey().getString("prev_file_path") ;
                                }else {
                                    path = m.getKey().getString("curr_file_path") ;
                                    System.out.println("==========DELETE1====================");
                                    System.out.println(metaPath);
                                    System.out.println("===========DELETE1===================");
                                }
                                prevFilePath = pathPrefix + "/" +   path;
                                deleteFilesList.add(isWindows ? pathUnixToWin(prevFilePath) : prevFilePath);
                            }catch (NullPointerException e) {
                                System.out.println("============DELETE2==================");
                                System.out.println(metaPath);
                                System.out.println("=============DELETE2=================");
                            }
                        }
                        if ("MODIFY".equals(m.getValue())) {
                            prevFilePath = pathPrefix + "/" + m.getKey().getString("prev_file_path");
                            preFileList.add( isWindows ? pathUnixToWin(prevFilePath) : prevFilePath);
                            currFilePath = pathPrefix + "/" + m.getKey().getString("curr_file_path");
                            curFileList.add( isWindows ? pathUnixToWin(currFilePath) : currFilePath);


                            if (m.getKey().containsKey("diffPath")) {
                                String diffPath = m.getKey().getString("diffPath");
                                //diffPath = isWindows ? pathUnixToWin(diffPath) : diffPath;
                                fileNameList.add(m.getKey().getString("file_full_name"));
                                diffPathList.add(isWindows ? pathUnixToWin(diffPath) : diffPath);
                            }else {
                                System.out.println("==========change====================");
                                System.out.println(metaPath);
                                System.out.println("===========change===================");
                            }
                        }
                    }
                }
                // RepoInfoBuilder need to refactor for parentCommit is not null; so
                RepoInfoBuilder preRepoInfo = new RepoInfoBuilder(repoUuid, preCommit, preFileList, jGitHelper, branch, null);
                RepoInfoBuilder curRepoInfo = new RepoInfoBuilder(repoUuid, preCommit, curFileList, jGitHelper, branch, preCommit);
                AnalyzeDiffFile analyzeDiffFile = new AnalyzeDiffFile(packageDao, fileDao, classDao, fieldDao, methodDao, preRepoInfo, curRepoInfo);
                analyzeDiffFile.addInfoConstruction(addFilesList);
                analyzeDiffFile.deleteInfoConstruction(deleteFilesList);
                analyzeDiffFile.modifyInfoConstruction(preRepoInfo, fileNameList, curRepoInfo, diffPathList);
                analyzeDiffFiles.add(analyzeDiffFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return analyzeDiffFiles;
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
/*    private String deletePrefix(String curPathList, String preCommit) {
        String p = curPathList.replace(preCommit, "--------");

        String s[] = p.split("--------");
        System.out.println(s);
        return pathUnixToWin(s[1]);
    }*/

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