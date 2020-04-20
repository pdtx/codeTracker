package cn.edu.fudan.codetracker.service;


/**
 * description 扫描接口
 * @author fancying
 */
public interface ScanService {

    //void scan(String repoUuid, String commitId, String branch, JGitHelper jGitHelper);


    void firstScan(String repoUuid, String branch, String duration);

    /**
     * description
     *
     * @param beginCommit 开始扫描的commit
     * @param branch 项目分值
     * @param repoUuid 代码仓库的 uuid
     */
    void scan(String repoUuid, String branch, String beginCommit);

    void autoUpdate(String repoUuid, String branch);

    String getScanStatus(String repoId, String branch);

}
