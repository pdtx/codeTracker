package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import lombok.*;

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
@Builder
@NoArgsConstructor
public class PackageNode extends BaseNode {

    private String packageName;
    private String moduleName;

    public PackageNode(String moduleName, String packageName) {
        super.setProjectInfoLevel(ProjectInfoLevel.PACKAGE);
        this.moduleName = moduleName;
        this.packageName = packageName;
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
            return this.getRootUuid().equals(packageNode.getRootUuid()) ;
        }
        return false;
    }

}