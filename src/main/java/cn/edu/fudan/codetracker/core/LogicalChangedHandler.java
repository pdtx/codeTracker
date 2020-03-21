package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;

/**
 * description: 增加的文件映射
 *
 * @author fancying
 * create: 2020-03-20 19:27
 **/
public class LogicalChangedHandler implements NodeMapping {

    private LogicalChangedHandler(){}

    static LogicalChangedHandler getInstance(){
        return MappingGeneratorHolder.LOGICAL_CHANGED_HANDLER;
    }

    private static final class MappingGeneratorHolder {
        private static final LogicalChangedHandler LOGICAL_CHANGED_HANDLER = new LogicalChangedHandler();
    }

    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot) {

    }
}