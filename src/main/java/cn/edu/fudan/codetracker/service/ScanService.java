package cn.edu.fudan.codetracker.service;


import cn.edu.fudan.codetracker.jgit.JGitHelper;

import java.util.List;

public interface ScanService {

    boolean scan(String repoUuid, String commitId, String branch, JGitHelper jGitHelper);

    void firstScan(String repoUuid, String branch, String duration);

    Object getMethodHistory(String repoId, String moduleName, String packageName, String className, String signature);
}
