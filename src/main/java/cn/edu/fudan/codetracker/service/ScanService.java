package cn.edu.fudan.codetracker.service;


public interface ScanService {

    //void scan(String repoUuid, String commitId, String branch, JGitHelper jGitHelper);


    void firstScan(String repoUuid, String branch, String duration);

    void autoScan(String repoUuid, String branch, String beginCommit);

    void autoUpdate(String repoUuid, String branch);

    String getScanStatus(String repoId, String branch);

}
