package cn.edu.fudan.codetracker.service;


import java.util.List;

public interface ScanService {

    boolean scan(String repoPath, String commitId, String outputDir);

    void firstScan(String repoUuid, List<String> commitList, String branch);

}
