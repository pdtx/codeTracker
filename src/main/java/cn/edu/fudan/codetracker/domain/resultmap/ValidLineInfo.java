package cn.edu.fudan.codetracker.domain.resultmap;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidLineInfo {
    private String uuid;
    private String committer;
    private String metaUuid;
    private String changeRelation;
    private String repoUuid;
    private String commitDate;
    private String body;
    private int begin;
    private int end;

    public ValidLineInfo() {

    }

}
