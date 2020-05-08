package cn.edu.fudan.codetracker.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * description: save repo info
 *
 * @author fancying
 * create: 2020-04-24 10:38
 **/
@Data
@Builder
@AllArgsConstructor
public class RepoResourceDTO {
    String repoId ;
    String repoPath ;

    public RepoResourceDTO() {
        repoId = "";
        repoPath = "";
    }

}