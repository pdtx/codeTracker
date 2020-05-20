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
                NodeMapping.setNodeMapped(null,baseNode,proxyDao,commonInfo);
                for (BaseNode child: baseNode.getChildren()) {
                    stack.push(child);
                }
            }
        } else {
            //当curRoot是除了文件节点以外其他节点时，判断其孩子是否为move
            while (!stack.empty()) {
                BaseNode baseNode = stack.pop();
                baseNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                NodeMapping.setNodeMapped(null,baseNode,proxyDao,commonInfo);
                if (baseNode.getChildren() != null) {
                    for (BaseNode child: baseNode.getChildren()) {
                        if (isAdd(child)) {
                            stack.push(child);
                        } else {
                            dealWithNotAdd(child);
                        }
                    }
                }
            }
        }
    }

    //后续完善，判断非文件节点的子节点是否为真正的ADD
    private boolean isAdd(BaseNode baseNode) {
        return true;
    }

    //问题：如果非ADD子节点的孩子为ADD怎么办？
    private void dealWithNotAdd(BaseNode baseNode) {
        //处理非ADD子节点
    }
}