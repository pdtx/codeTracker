package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FileNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Stack;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-20 19:31
 **/
public class DeleteHandler implements NodeMapping {
    private ProxyDao proxyDao;

    private DeleteHandler(){}

    @Autowired
    public void setProxyDao(ProxyDao proxyDao) {
        this.proxyDao = proxyDao;
    }

    public static DeleteHandler getInstance() {
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
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo) {
        Stack<BaseNode> stack = new Stack<>();
        stack.push(preRoot);
        if (preRoot instanceof FileNode) {
            while (!stack.empty()) {
                BaseNode baseNode = stack.pop();
                baseNode.setChangeStatus(BaseNode.ChangeStatus.DELETE);
                NodeMapping.setNodeMapped(preRoot,null,proxyDao,commonInfo);
                for (BaseNode child : baseNode.getChildren()) {
                    stack.push(child);
                }
            }
        } else {
            while (!stack.empty()) {
                BaseNode baseNode = stack.pop();
                baseNode.setChangeStatus(BaseNode.ChangeStatus.DELETE);
                NodeMapping.setNodeMapped(preRoot,null,proxyDao,commonInfo);
                for (BaseNode child : baseNode.getChildren()) {
                    if (isDelete(child)) {
                        stack.push(child);
                    } else {
                        dealWithNotDelete(child);
                    }
                }
            }
        }
    }

    //后续完善，判断非文件节点的子节点是否为真正的DELETE
    private boolean isDelete(BaseNode baseNode) {
        return true;
    }

    //问题：如果非DELETE子节点的孩子为DELETE怎么办？
    private void dealWithNotDelete(BaseNode baseNode) {
        //处理非DELETE子节点
    }

}