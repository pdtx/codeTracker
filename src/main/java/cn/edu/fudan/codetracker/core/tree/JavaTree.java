package cn.edu.fudan.codetracker.core.tree;

import cn.edu.fudan.codetracker.core.tree.parser.FileParser;

import java.util.List;

/**
 * description: java语言树
 *
 * @author fancying
 * create: 2020-05-18 00:22
 **/
public class JavaTree extends BaseLanguageTree {

    public JavaTree(List<String> fileList, FileParser fileParser) {
        super(fileList, fileParser);
    }


    @Override
    public void parseTree() {

    }
}