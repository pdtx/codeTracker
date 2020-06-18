package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.constants.ScanStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author chenyuan
 * 2020-06-18
 */
@Getter
@Setter
public class ScanInfo {
    private String uuid;
    private String status;
    private int totalCommitCount;
    private int scannedCommitCount;
    private Date startScanTime;
    private Date endScanTime;
    /**
     * 总耗时 单位s
     */
    private long scanTime;
    private String latestCommit;
    private String branch;
    private String repoId;

    public ScanInfo () {

    }

    public ScanInfo (String uuid, String status, int totalCommitCount, int scannedCommitCount, Date startScanTime, String repoId, String branch) {
        this.uuid = uuid;
        this.status = status;
        this.totalCommitCount = totalCommitCount;
        this.scannedCommitCount = scannedCommitCount;
        this.startScanTime = startScanTime;
        this.repoId = repoId;
        this.branch = branch;
    }

}
