package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-21 16:26
 **/
@Getter
@Setter
@NoArgsConstructor
public class PackageNode extends BaseNode {

    private String packageName;
    private String moduleName;

    private List<FileNode> fileNodes;

    public PackageNode(String moduleName, String packageName) {
        super.setProjectInfoLevel(ProjectInfoLevel.PACKAGE);
        this.moduleName = moduleName;
        this.packageName = packageName;
        this.fileNodes = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleName, packageName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof PackageNode){
            PackageNode packageNode = (PackageNode) o;
            return this.packageName.equals(packageNode.getPackageName()) &&
                    this.moduleName.equals(packageNode.getModuleName());
        }
        return false;
    }

    public List<FileNode> getFileNodes() {
        return fileNodes;
    }

    public void setFileNodes(List<FileNode> fileNodes) {
        this.fileNodes = fileNodes;
    }
}