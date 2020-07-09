package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * description: 匹配物理上的代码变更
 *
 * @author fancying
 * create: 2020-03-21 20:32
 **/
@Slf4j
public class PhysicalChangedHandler implements NodeMapping{

    private PhysicalChangedHandler() {}

    public static PhysicalChangedHandler getInstance() {
        return MappingGeneratorHolder.PHYSICAL_CHANGED_HANDLER;
    }

    private static final class MappingGeneratorHolder{
        private static final PhysicalChangedHandler PHYSICAL_CHANGED_HANDLER = new PhysicalChangedHandler();
    }
    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo preCommonInfo, CommonInfo curCommonInfo, ProxyDao proxyDao) {
        if (preRoot == null || curRoot == null) {
            log.error("root node  is null");
            return;
        }

        if (! preRoot.getClass().equals(curRoot.getClass())) {
            log.error("class type is inconsistent");
            return;
        }

        //如果为文件节点
        if(preRoot instanceof FileNode) {
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
                    boolean isSameLine = preMethod.getBegin() == curMethod.getBegin() && preMethod.getEnd() == curMethod.getEnd();
                    boolean isSameContent = preMethod.getContent().equals(curMethod.getContent());
                    if (!isSameLine || !isSameContent) {
                        BaseNode.ChangeStatus c = isSameContent ?  BaseNode.ChangeStatus.CHANGE_LINE : BaseNode.ChangeStatus.CHANGE_RECORD;
                        curMethod.setChangeStatus(c);
                    }
                }
                if (preNode instanceof StatementNode && curNode instanceof StatementNode) {
                    StatementNode preStatement = (StatementNode)preNode;
                    StatementNode curStatement = (StatementNode)curNode;
                    boolean isSameLine = preStatement.getBegin() == curStatement.getBegin() && preStatement.getEnd() == curStatement.getEnd();
                    boolean isSameContent = preStatement.getBody().equals(curStatement.getBody());
                    if (!isSameLine || !isSameContent) {
                        // fixme curStatement.setIsLogic(0) ??
                        curStatement.setIsLogic(0);
                        BaseNode.ChangeStatus c = isSameContent ?  BaseNode.ChangeStatus.CHANGE_LINE : BaseNode.ChangeStatus.CHANGE_RECORD;
                        curStatement.setChangeStatus(c);
                    }
                }
                // fixme 物理上的改变是否需要 version + 1 ？
                // todo 只有行号变的话 version应该不变
                NodeMapping.setNodeMapped(preNode, curNode, proxyDao, preCommonInfo, curCommonInfo);
                //物理改变版本号不变 如果trackerInfo拿不到，处理为ADD，版本号为1情况需排除
                if (curNode.getVersion() > 1) {
                    curNode.setVersion(curNode.getVersion()-1);
                }
                preNode.getChildren().forEach(preQueue::offer);
                curNode.getChildren().forEach(curQueue::offer);
            }
            return;
        }
        // fixme 不递归遍历其他的节点是否是逻辑上的改变？
        if (preRoot instanceof MethodNode  || preRoot instanceof StatementNode) {
            if (preRoot instanceof MethodNode) {
                MethodNode curMethod = (MethodNode)curRoot;
                MethodNode preMethod = (MethodNode)preRoot;
                BaseNode.ChangeStatus changeStatus = curMethod.getContent().equals(preMethod.getContent()) ? BaseNode.ChangeStatus.CHANGE_LINE : BaseNode.ChangeStatus.CHANGE_RECORD;
                curRoot.setChangeStatus(changeStatus);
            }
            if (preRoot instanceof StatementNode) {
                StatementNode curStatement = (StatementNode)curRoot;
                StatementNode preStatement = (StatementNode)preRoot;
                BaseNode.ChangeStatus changeStatus = curStatement.getBody().equals(preStatement.getBody()) ? BaseNode.ChangeStatus.CHANGE_LINE : BaseNode.ChangeStatus.CHANGE_RECORD;
                curRoot.setChangeStatus(changeStatus);
                curStatement.setIsLogic(0);
            }
            NodeMapping.setNodeMapped(preRoot, curRoot, proxyDao, preCommonInfo, curCommonInfo);
            //物理改变版本号不变 如果trackerInfo拿不到，处理为ADD，版本号为1情况需排除
            if (curRoot.getVersion() > 1) {
                curRoot.setVersion(curRoot.getVersion() - 1);
            }
        }
    }
}