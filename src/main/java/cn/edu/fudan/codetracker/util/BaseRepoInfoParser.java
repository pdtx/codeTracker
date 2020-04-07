package cn.edu.fudan.codetracker.util;

import cn.edu.fudan.codetracker.domain.projectinfo.ClassNode;
import cn.edu.fudan.codetracker.domain.projectinfo.FieldNode;
import cn.edu.fudan.codetracker.domain.projectinfo.MethodNode;
import cn.edu.fudan.codetracker.domain.projectinfo.StatementNode;

import java.util.List;

/**
 * 接口功能：所有语言通用的结构转化
 * package、class、method、field、statement
 */
public interface BaseRepoInfoParser {

    /**
     * 转化moduleName
     * @param singleDir
     * @return
     */
    String parseModuleName(String[] singleDir);

    /**
     * 转化packageName
     * @return
     */
    String parsePackageName();

    /**
     * 转化class或者interface
     * @return
     */
    void parseClassOrInterface();

    /**
     * 转化field
     * @return
     */
    List<FieldNode> parseField(ClassNode classNode);

    /**
     * 转化构造方法（构造方法也属于method）
     * @return
     */
    List<MethodNode> parseConstructors(ClassNode classNode);

    /**
     * 转化method
     * @return
     */
    List<MethodNode> parseMethod(ClassNode classNode);

    /**
     * 转化父亲节点为method的statement
     * @return
     */
    List<StatementNode> parseLevelOneStmt(MethodNode methodNode);

    /**
     * 转化父亲节点为statement的statement
     * @return
     */
    List<StatementNode> parseLevelTwoStmt(MethodNode methodNode, StatementNode parent);

}
