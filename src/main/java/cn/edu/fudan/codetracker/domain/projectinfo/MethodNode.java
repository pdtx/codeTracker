package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.util.comparison.CosineUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String lastChangeCommit;
    private int ccn;

    public MethodNode() {
        super.setProjectInfoLevel(ProjectInfoLevel.METHOD);
        this.diff = new JSONObject();
        this.diff.put("data",new JSONArray());
    }

    public String getClassName() {
        ClassNode classNode = (ClassNode) super.getParent();
        return classNode.getClassName();
    }

    public String getPackageName() {
        ClassNode classNode = (ClassNode) super.getParent();
        return classNode.getPackageName();
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

    public static Map<MethodNode, Double> findMostSimilarMethod(MethodNode target, List<MethodNode> methodNodeList) {
        Map<MethodNode, Double> map = new HashMap<>(2);
        double threshold = 0.6;
        for (MethodNode m : methodNodeList) {
            double tmp = CosineUtil.cosineSimilarity(target.getContent(), m.getContent());
            if (tmp > threshold) {
                map.clear();
                threshold = tmp;
                map.put(m, tmp);
            }
        }
        return map;
    }

    public boolean isChangeCalledMethod() {
        return ChangeStatus.ADD.equals(this.getChangeStatus())
                || ChangeStatus.DELETE.equals(this.getChangeStatus())
                || ChangeStatus.SELF_CHANGE.equals(this.getChangeStatus())
                || ChangeStatus.CHANGE.equals(this.getChangeStatus());
    }
}
