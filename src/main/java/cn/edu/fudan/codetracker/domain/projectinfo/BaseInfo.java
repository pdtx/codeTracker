/**
 * @description: super class of all project info class
 * @author: fancying
 * @create: 2019-11-11 20:18
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import java.util.Date;

public class BaseInfo {

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

    BaseInfo() {

    }
}