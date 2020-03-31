package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class FileNode extends BaseNode{

    private String fileName;
    private String filePath;

    public FileNode(String fileName, String filePath) {
        super.setProjectInfoLevel(ProjectInfoLevel.FILE);
        this.fileName = fileName;
        this.filePath = filePath;
    }

    /**
     *filePath 可以唯一指定一个file
     */
    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if(obj == null){
            return false;
        }

        if(obj instanceof FileNode){
            FileNode fileNode = (FileNode) obj;
            return this.filePath.equals(fileNode.filePath);
        }
        return false;
    }
}
