package cn.edu.fudan.codetracker.service;


import cn.edu.fudan.codetracker.jgit.JGitHelper;

import java.util.List;

public interface ScanService {

    boolean scan(String repoUuid, String commitId, String branch, String outputDir, JGitHelper jGitHelper);

    void firstScan(String repoUuid, List<String> commitList, String branch);

}
