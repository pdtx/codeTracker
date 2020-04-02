package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class MethodNode extends BaseNode{

    private String fullName;
    private String signature;
    private String content;
    private String modifier;
    private String primitiveType;
    private int begin;
    private int end;
    private JSONObject diff;
    private String packageName;
    private String filePath;

    public MethodNode() {
        super.setProjectInfoLevel(ProjectInfoLevel.METHOD);
        this.diff = new JSONObject();
        this.diff.put("data",new JSONArray());
    }

    public String getClassName() {
        ClassNode classNode = (ClassNode) super.getParent();
        return classNode.getClassName();
    }

    @Override
    public int hashCode() {
        return (filePath + getClassName() + signature).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof MethodNode){
            MethodNode methodNode = (MethodNode) o;
            return this.filePath.equals(methodNode.getFilePath()) &&
                    this.getClassName().equals(methodNode.getClassName()) &&
                    this.signature.equals(methodNode.signature);
        }
        return false;
    }


}
