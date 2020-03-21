package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-20 19:31
 **/
public class DeleteHandler implements NodeMapping {

    private DeleteHandler(){}

    static DeleteHandler getInstance() {
        return SingletonEnum.SINGLETON_ENUM.getDeleteHandler();
    }

    enum SingletonEnum {
        // 创建单例
        SINGLETON_ENUM;
        private DeleteHandler deleteHandler;
        SingletonEnum() {
            deleteHandler = new DeleteHandler();
        }
        public DeleteHandler getDeleteHandler() {
            return deleteHandler;
        }
    }
    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot) {

    }

}