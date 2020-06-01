package cn.edu.fudan.codetracker.core.tree;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * description: 基于C++语言构造的项目结果树
 *
 * @author fancying
 * create: 2020-05-18 09:17
 **/
@Component("cpp")
@Scope("prototype")
@Data
public class CppTree extends BaseLanguageTree{

    public CppTree(List<String> fileList, String repoUuid) {
        super(fileList, repoUuid);
    }

    @Override
    public void parseTree() {

    }
}