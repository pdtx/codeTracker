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
@NoArgsConstructor
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

    public MethodNode(String signature, String modifier) {
        super.setProjectInfoLevel(ProjectInfoLevel.METHOD);
        this.signature = signature;
        this.modifier = modifier;
        this.diff = new JSONObject();
        this.diff.put("data",new JSONArray());
        ClassNode classNode = (ClassNode)super.getParent();
        this.packageName = classNode.getPackageName();
        this.filePath = classNode.getFilePath();
    }

    @Override
    public int hashCode() {
        ClassNode classNode = (ClassNode) super.getParent();
        return (classNode.getFilePath() + classNode.getClassName() + signature).hashCode();
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
            return this.getUuid().equals(methodNode.getUuid());
        }
        return false;
    }


}
