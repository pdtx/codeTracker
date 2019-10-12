package cn.edu.fudan.codetracker.service;


import cn.edu.fudan.codetracker.jgit.JGitHelper;

import java.util.List;

public interface ScanService {

    boolean scan(String repoPath, String commitId, String outputDir, JGitHelper jGitHelper);

    void firstScan(String repoUuid, List<String> commitList, String branch);

}
