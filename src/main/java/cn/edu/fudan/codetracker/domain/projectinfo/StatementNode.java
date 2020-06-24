package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.StatementType;
import cn.edu.fudan.codetracker.util.comparison.CosineUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.jgit.util.StringUtils;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class StatementNode extends BaseNode{

    private String body;
    private int begin;
    private int end;

    private int level;
    private StatementType type;
    private int sequence;
    private String methodUuid;
    private String description;

    /**
     * 除去孩子的token
     */
    private List<Object> selfBodyToken;
    /**
     * 是否为逻辑修改
     * 1 for true
     * 0 for false
     */
    private int isLogic = 1;

    public StatementNode (String body, int begin, int end) {
        super.setProjectInfoLevel(ProjectInfoLevel.STATEMENT);
        this.body = body;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        //如何判断两个statement对应
        if(o instanceof StatementNode){
            StatementNode statementNode = (StatementNode) o;
            return this.getBody().equals(statementNode.getBody()) &&
                    this.getLevel() == statementNode.getLevel();
        }
        return false;
    }

    public List<Object> getSelfBodyToken() {
        if (selfBodyToken != null) {
            return selfBodyToken;
        }

        String p = CosineUtil.diffBody(body);

        List<? extends BaseNode> children = super.getChildren();
        if (children == null || children.size() == 0) {
            selfBodyToken =  Arrays.asList(p.split(","));
            return selfBodyToken;
        }

        for (BaseNode b : children) {
            StatementNode s = (StatementNode)b;
            String subSet = CosineUtil.diffBody(s.getBody());
            p = CosineUtil.diff(p, subSet);
        }
        selfBodyToken =  Arrays.asList(p.split(","));
        return selfBodyToken;
    }

    public static Map<StatementNode, Double>  findMostSimilarStatement(StatementNode target, List<StatementNode> nodeList) {
        double similarity = 0.6;
        Map<StatementNode, Double> result = new HashMap<>(2);
        for (StatementNode statement : nodeList) {
            if (statement.getLevel() != target.getLevel()) {
                continue;
            }
            double tmp = CosineUtil.cosineSimilarity(target.getBody(), statement.getBody());
            if (tmp > similarity) {
                result.clear();
                similarity = tmp;
                result.put(statement, similarity);
            }
        }

        return result;
    }

}
