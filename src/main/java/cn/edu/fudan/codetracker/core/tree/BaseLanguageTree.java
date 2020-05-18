package cn.edu.fudan.codetracker.core.tree;

import cn.edu.fudan.codetracker.core.tree.parser.FileParser;
import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;
import cn.edu.fudan.codetracker.domain.projectinfo.FileNode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description: 特定语言的项目结构树 每一种语言都对应一种parser
 *
 * @author fancying
 * create: 2020-05-17 15:09
 **/
@Getter
@NoArgsConstructor
public abstract class BaseLanguageTree {

    private FileParser parser;
    private List<String> fileList;
    private BaseNode root;

    BaseLanguageTree(List<String> fileList, FileParser fileParser) {
        this.fileList = fileList;
        this.parser = fileParser;
    }

    public abstract void parseTree();

}
