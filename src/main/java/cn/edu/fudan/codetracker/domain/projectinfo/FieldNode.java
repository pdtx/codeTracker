package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FieldNode extends BaseNode{

    private String fullName;
    private String simpleName;
    private String modifier;
    private String simpleType;
    private String initValue;
    private String filePath;

    private int begin;
    private int end;

    public FieldNode(String simpleName, String modifier, String simpleType, String initValue){
        super.setProjectInfoLevel(ProjectInfoLevel.FIELD);
        this.simpleName = simpleName;
        this.modifier = modifier;
        this.simpleType = simpleType;
        this.initValue = initValue;
    }

    public String getClassName(){
        ClassNode classNode = (ClassNode)this.getParent();
        return classNode.getClassName();
    }

    @Override
    public int hashCode() {
        return (this.simpleName + this.modifier + this.simpleType + this.filePath).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if(obj == null){
            return false;
        }

        if(obj instanceof FieldNode){
            FieldNode fieldNode = (FieldNode) obj;
            return this.simpleName.equals(fieldNode.getSimpleName()) &&
                    this.modifier.equals(fieldNode.getModifier()) &&
                    this.simpleType.equals(fieldNode.getSimpleType()) &&
                    this.filePath.equals(fieldNode.getFilePath());
        }
        return false;
    }


    public boolean isSame(FieldNode fieldNode) {
        if (this == fieldNode) {
            return true;
        }

        if(fieldNode == null){
            return false;
        }
        return this.simpleName.equals(fieldNode.getSimpleName()) &&
                this.modifier.equals(fieldNode.getModifier()) &&
                this.simpleType.equals(fieldNode.getSimpleType()) ;
    }
}
