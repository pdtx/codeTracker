package cn.edu.fudan.codetracker.core.tree.parser;

/**
 * description: 语言解析接口
 *
 * @author fancying
 * create: 2020-05-17 15:13
 **/
public interface FileParser {
    void parse(String path, String relativePath, String projectName);
}
