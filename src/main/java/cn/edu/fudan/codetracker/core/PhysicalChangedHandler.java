package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-21 20:32
 **/
public class PhysicalChangedHandler implements NodeMapping{

    private PhysicalChangedHandler() {}

    static PhysicalChangedHandler getInstance() {
        return MappingGeneratorHolder.PHYSICAL_CHANGED_HANDLER;
    }

    private static final class MappingGeneratorHolder{
        private static final PhysicalChangedHandler PHYSICAL_CHANGED_HANDLER = new PhysicalChangedHandler();
    }
    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot) {

    }
}