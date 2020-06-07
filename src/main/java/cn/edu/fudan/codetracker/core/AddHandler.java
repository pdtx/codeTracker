package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;
import cn.edu.fudan.codetracker.domain.projectinfo.ClassNode;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FileNode;

import java.util.Stack;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-21 00:24
 **/
public class AddHandler implements NodeMapping {

    private AddHandler(){}

    public static AddHandler getInstance(){
        return MappingGeneratorHolder.ADD_HANDLER;
    }

    private static final class MappingGeneratorHolder {
        static final AddHandler ADD_HANDLER = new AddHandler();
    }


    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo, ProxyDao proxyDao) {
        Stack<BaseNode> stack = new Stack<>();
        stack.push(curRoot);
        if (curRoot instanceof FileNode) {
            while (!stack.empty()) {
                BaseNode baseNode = stack.pop();
                baseNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                NodeMapping.setNodeMapped(null, baseNode, proxyDao, commonInfo);
                pushChildrenInto(baseNode, stack);
            }
            return;
        }

        //当curRoot是除了文件节点以外其他节点时，判断其孩子是否为move
        // singleNodeAddMapping(preRoot, curRoot, commonInfo, proxyDao);
        // todo 处理当前节点 不处理子节点
        curRoot.setChangeStatus(BaseNode.ChangeStatus.ADD);
        NodeMapping.setNodeMapped(null, curRoot, proxyDao, commonInfo);
    }

    private void singleNodeAddMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo, ProxyDao proxyDao) {
        Stack<BaseNode> stack = new Stack<>();
        stack.push(curRoot);
        while (!stack.empty()) {
            BaseNode baseNode = stack.pop();
            baseNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
            NodeMapping.setNodeMapped(null, baseNode, proxyDao, commonInfo);
            if (baseNode.getChildren() != null) {
                for (BaseNode child: baseNode.getChildren()) {
                    if (isAdd(child)) {
                        stack.push(child);
                    } else {
                        dealWithNotAdd(child);
                    }
                }
            }
            if (baseNode instanceof ClassNode) {
                ClassNode classNode = (ClassNode)baseNode;
                if (classNode.getFieldNodes() != null) {
                    for (BaseNode field : classNode.getFieldNodes()) {
                        if (isAdd(field)) {
                            stack.push(field);
                        } else {
                            dealWithNotAdd(field);
                        }
                    }
                }
            }
        }
    }

    /**
     * todo 后续完善，判断非文件节点的子节点是否为真正的ADD
     */
    private boolean isAdd(BaseNode baseNode) {
        return true;
    }

    /**
     * fixme 问题：如果非ADD子节点的孩子为ADD怎么办？
     */
    private void dealWithNotAdd(BaseNode baseNode) {
        //处理非ADD子节点
    }
    
    static void pushChildrenInto(BaseNode baseNode, Stack<BaseNode> stack){
        if (baseNode.getChildren() != null) {
            baseNode.getChildren().forEach(stack::push);
        }
        if (baseNode instanceof ClassNode ) {
            ClassNode classNode = (ClassNode)baseNode;
            if (classNode.getFieldNodes() != null) {
                classNode.getFieldNodes().forEach(stack::push);
            }
        }
    }
}