package cn.edu.fudan.codetracker.domain.resultmap;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MethodHistory {
    private String commit;
    private String committer;
    private String commitMessage;
    private String commitDate;
    private String content;
    private com.alibaba.fastjson.JSONObject diff;
    private String changeRelation;
    private int methodBegin;
    private int methodEnd;
    private String parentCommit;
    private String repoUuid;
    private String filePath;
    private int line;


    public MethodHistory() {

    }

}
