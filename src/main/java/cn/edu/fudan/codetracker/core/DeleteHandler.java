package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;
import cn.edu.fudan.codetracker.domain.projectinfo.ClassNode;
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

    private DeleteHandler(){}

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
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo, ProxyDao proxyDao) {
        Stack<BaseNode> stack = new Stack<>();
        stack.push(preRoot);
        if (preRoot instanceof FileNode || preRoot instanceof ClassNode) {
            while (!stack.empty()) {
                BaseNode baseNode = stack.pop();
                baseNode.setChangeStatus(BaseNode.ChangeStatus.DELETE);
                NodeMapping.setNodeMapped(baseNode,null, proxyDao, commonInfo);
                NodeMapping.pushChildrenIntoStack(baseNode, stack);
            }
        }

        // fixme 处理当前method statement节点 子节点目前也处理为删除，暂不考虑move
        while (!stack.empty()) {
            BaseNode baseNode = stack.pop();
            baseNode.setChangeStatus(BaseNode.ChangeStatus.DELETE);
            NodeMapping.setNodeMapped(baseNode,null, proxyDao, commonInfo);
            NodeMapping.pushChildrenIntoStack(baseNode, stack);
        }
    }

    private void singleNodeAddMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo, ProxyDao proxyDao) {
        Stack<BaseNode> stack = new Stack<>();
        stack.push(preRoot);
        while (!stack.empty()) {
            BaseNode baseNode = stack.pop();
            baseNode.setChangeStatus(BaseNode.ChangeStatus.DELETE);
            NodeMapping.setNodeMapped(preRoot,null, proxyDao, commonInfo);
            if (baseNode.getChildren() != null) {
                for (BaseNode child : baseNode.getChildren()) {
                    if (isDelete(child)) {
                        stack.push(child);
                    } else {
                        dealWithNotDelete(child);
                    }
                }
            }
            if (baseNode instanceof ClassNode) {
                ClassNode classNode = (ClassNode)baseNode;
                if (classNode.getFieldNodes() != null) {
                    for (BaseNode field : classNode.getFieldNodes()) {
                        if (isDelete(field)) {
                            stack.push(field);
                        } else {
                            dealWithNotDelete(field);
                        }
                    }
                }
            }
        }
    }

    /**
     *后续完善，判断非文件节点的子节点是否为真正的DELETE
     */
    private boolean isDelete(BaseNode baseNode) {
        return true;
    }

    /**
     *问题：如果非DELETE子节点的孩子为DELETE怎么办？
     */
    private void dealWithNotDelete(BaseNode baseNode) {
        //处理非DELETE子节点
    }

}