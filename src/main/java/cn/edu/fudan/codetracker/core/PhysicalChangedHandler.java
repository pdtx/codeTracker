package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.Node;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * description:
 *
 * @author fancying
 * create: 2020-03-21 20:32
 **/
public class PhysicalChangedHandler implements NodeMapping{
    private ProxyDao proxyDao;

    private PhysicalChangedHandler() {}

    @Autowired
    public void setProxyDao(ProxyDao proxyDao) {
        this.proxyDao = proxyDao;
    }

    public static PhysicalChangedHandler getInstance() {
        return MappingGeneratorHolder.PHYSICAL_CHANGED_HANDLER;
    }

    private static final class MappingGeneratorHolder{
        private static final PhysicalChangedHandler PHYSICAL_CHANGED_HANDLER = new PhysicalChangedHandler();
    }
    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo) {
        //如果为文件节点
        if(preRoot instanceof FileNode && curRoot instanceof FileNode) {
            Queue<BaseNode> preQueue = new ArrayDeque<>();
            Queue<BaseNode> curQueue = new ArrayDeque<>();
            preQueue.offer(preRoot);
            curQueue.offer(curRoot);
            while (preQueue.size() != 0 && curQueue.size() != 0) {
                BaseNode preNode = preQueue.poll();
                BaseNode curNode = curQueue.poll();
                //物理改变目前只考虑method、statement行号更新
                if (preNode instanceof MethodNode && curNode instanceof MethodNode) {
                    MethodNode preMethod = (MethodNode)preNode;
                    MethodNode curMethod = (MethodNode)curNode;
                    if (preMethod.getBegin() != curMethod.getBegin() || preMethod.getEnd() != curMethod.getEnd()) {
                        curMethod.setChangeStatus(BaseNode.ChangeStatus.CHANGE_RECORD);
                    } else {
                        curMethod.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                    }
                } else if (preNode instanceof StatementNode && curNode instanceof StatementNode) {
                    StatementNode preStatement = (StatementNode)preNode;
                    StatementNode curStatement = (StatementNode)curNode;
                    if (preStatement.getBegin() != curStatement.getBegin() || preStatement.getEnd() != curStatement.getEnd()) {
                        curStatement.setChangeStatus(BaseNode.ChangeStatus.CHANGE_RECORD);
                    } else {
                        curStatement.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                    }
                } else {
                    curNode.setChangeStatus(BaseNode.ChangeStatus.UNCHANGED);
                }
                NodeMapping.setNodeMapped(preNode,curNode,proxyDao,commonInfo);
                for (BaseNode baseNode : preNode.getChildren()) {
                    preQueue.offer(baseNode);
                }
                for (BaseNode baseNode : curNode.getChildren()) {
                    curQueue.offer(baseNode);
                }
            }
        }
    }
}