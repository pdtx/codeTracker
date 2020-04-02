package cn.edu.fudan.codetracker.domain.projectinfo;

import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * description: 在一个版本结构树中 所有节点所共有的属性
 * @author fancying
 * create: 2019-11-11 20:18
 **/
@Getter
@Setter
public class CommonInfo {

    private String startCommit;
    private Date startCommitDate ;
    private String endCommit;
    private Date endCommitDate ;

    private String repoUuid;
    private String branch;
    private Date commitDate ;
    private String commit;
    private String committer;
    private String commitMessage;
    /**
     * just parent commit not previous version commit
     */
    private String parentCommit;


    /**
     * 存储的是相对文件地址
     */

    private List<String> deleteFile;
    private List<String> addFile;
    /**
     * 非逻辑上改变的文件
     * 比如：只增加了空行、注释等 并不会导致代码语义上的改变
     */
    private Map physicallyChangedFile;
    /**
     * 逻辑上改变的文件
     */
    private Map logicalChangedFile;

    /**
     *  TODO 过滤的文件暂时不填充
     * 过滤的文件 包括 test、enum、非java文件等
     */
    private List<String> filterFile;

    public CommonInfo() {

    }

    public CommonInfo(String repoUuid, String branch, String commit, Date commitDate, String committer, String commitMessage, String parentCommit) {
        this.repoUuid = repoUuid;
        this.branch = branch;
        this.commit = commit;
        this.commitDate = commitDate;
        this.committer = committer;
        this.commitMessage = commitMessage;
        this.parentCommit = parentCommit;
    }

}