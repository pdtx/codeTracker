package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.StatementType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    /**
     * 是否为逻辑修改
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

}
