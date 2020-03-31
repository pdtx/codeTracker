package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class ClassNode extends BaseNode{

    private String fullName;
    private String className;
    private String modifier;

    private int begin;
    private int end;

    /**
     * 目前数据库未存，不知是否有留下必要
     */
    private List<String> extendedList;
    private List<String> implementedList;

    /**
     * children里面默认放method
     */
    private List<FieldNode> fieldNodes;

    public ClassNode(String fullName, String className, String modifier, int begin, int end) {
        super.setProjectInfoLevel(ProjectInfoLevel.CLASS);
        this.fullName = fullName;
        this.className = className;
        this.modifier = modifier;
        this.begin = begin;
        this.end = end;
    }

    public String getFilePath() {
        FileNode fileNode = (FileNode)super.getParent();
        return fileNode.getFilePath();
    }

    @Override
    public int hashCode() {
        return (getFilePath() + className).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof ClassNode){
            ClassNode classNode = (ClassNode) o;
            return this.getUuid().equals(classNode.getUuid());
        }
        return false;
    }

}
