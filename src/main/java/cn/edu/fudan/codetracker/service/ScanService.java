package cn.edu.fudan.codetracker.service;


/**
 * description 扫描接口
 * @author fancying
 */
public interface ScanService {

    /**
     * description
     *
     * @param beginCommit 开始扫描的commit
     * @param branch 项目分值
     * @param repoUuid 代码仓库的 uuid
     */
    void scan(String repoUuid, String branch, String beginCommit);

    /**
     * description
     *
     * @param branch 项目分值
     * @param repoUuid 代码仓库的 uuid
     */
    void autoUpdate(String repoUuid, String branch);

    /**
     * description
     *
     * @param branch 项目分值
     * @param repoUuid 代码仓库的 uuid
     */
    String getScanStatus(String repoUuid, String branch);

}
