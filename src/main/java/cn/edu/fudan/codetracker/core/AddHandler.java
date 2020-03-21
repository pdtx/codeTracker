package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-21 00:24
 **/
public class AddHandler implements NodeMapping {

    private AddHandler(){}

    static AddHandler getInstance(){
        return MappingGeneratorHolder.ADD_HANDLER;
    }

    private static final class MappingGeneratorHolder {
        static final AddHandler ADD_HANDLER = new AddHandler();
    }


    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot) {

    }
}