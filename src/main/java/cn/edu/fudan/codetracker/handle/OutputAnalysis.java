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

    private String outputDir;
    private String commitId;
    private JGitHelper jGitHelper;

    public OutputAnalysis(String outputDir, JGitHelper jGitHelper) {
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

            ArrayList<String>  preFileList = new ArrayList<>();
            ArrayList<String>  curFileList = new ArrayList<>();
            // if preCommits include not only one record, then take into account merge
            // 后续再优化 如果一个文件在两个parent上都有修改
            preCommits = metaInfoJSON.getJSONArray("parents");
            for (int i = 0; i < preCommits.size(); i++) {
                // construct change file list according to preCommit
                String preCommit = preCommits.getString(i);
                for (Map.Entry<JSONObject, String> m : diffFileAction.entrySet()) {
                    if (m.getKey().getString("parent_commit").equals(preCommit)) {
                        String prevFilePath = metaPath.split("meta.json")[0] + "/" + m.getKey().getString("prev_file_path");
                        preFileList.add( isWindows ? pathUnixToWin(prevFilePath) : prevFilePath);
                    }
                    String currFilePath = metaPath.split("meta.json")[0] + "/" + m.getKey().getString("prev_file_path");
                    curFileList.add( isWindows ? pathUnixToWin(currFilePath) : currFilePath);
                }
                //RepoInfoBuilder preRepoInfo = new RepoInfoBuilder(repoUuid, commitList.get(0), repoPath, jGitHelper, branch, null);

                String preCommitter = jGitHelper.getAuthorName(preCommit);
            }


/*            for (int i = 0; i < preCommits.size(); i++) {
                String preCommit = preCommits.getString(i);
                String preCommitter = jGitHelper.getAuthorName(preCommit);
                jGitHelper.checkout(preCommit);
                RepoInfoBuilder preRepoInfo = new RepoInfoBuilder();
                ProjectInfoBuilder preProjectInfo = new ProjectInfoBuilder(projectName, preCommit, preCommitter, repoPath);
                AnalyzeDiffFile analyzeDiffFile = new AnalyzeDiffFile(preProjectInfo, curProjectInfo);
                jGitHelper.checkout(commitId);
                notChangedFileList = listJavaFiles(new File(repoPath));
                analyzeMetaInfoFiles(diffFileAction, analyzeDiffFile, preCommit, metaPath);
                analyzeDiffFile.packageRelationAnalyze(notChangedFileList);
                cqlSet.addAll(analyzeDiffFile.getCqlSet());
            }
            return new ArrayList<>(cqlSet);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void analyzeMetaInfoFiles(Map<JSONObject, String> diffFileAction, AnalyzeDiffFile analyzeDiffFile, String preCommit, String metaPath) {
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




/*        analyzeDiffFile.addInfoConstruction(addFilesList);
        analyzeDiffFile.deleteInfoConstruction(deleteFilesList);
        analyzeDiffFile.modifyInfoConstruction(prePathList, curPathList, diffPathList);

        analyzeDiffFile.packageRelationAnalyze(notChangedFileList);*/
    }
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