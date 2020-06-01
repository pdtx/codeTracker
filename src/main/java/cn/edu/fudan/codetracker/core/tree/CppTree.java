package cn.edu.fudan.codetracker.core.tree;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * description: 基于C++语言构造的项目结果树
 *
 * @author fancying
 * create: 2020-05-18 09:17
 **/
@Component("cpp")
@Scope("prototype")
public class CppTree extends BaseLanguageTree{
    public static final Language LANGUAGE = Language.CPP;

    @Override
    public void parseTree() {

    }
}