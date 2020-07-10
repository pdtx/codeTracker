package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.projectinfo.*;

import java.util.Stack;

/**
 * description: 此类主要负责节点间的映射
 *
 * @author fancying
 * create: 2020-03-18 09:04
 **/
public interface NodeMapping {

    /**
     * 两个节点树的映射
     * @param preRoot 前一个版本的根结点
     * @param curRoot 当前版本的根节点
     * @param commonInfo 公共信息
     * @param proxyDao  提供入库
     */
    void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo, ProxyDao proxyDao);

    /**
     * 设置两个节点的映射状态
     * @param preRoot 前一个版本的根结点
     * @param curRoot 当前版本的根节点
     * @param commonInfo 公共信息
     * @param proxyDao  提供入库
     */
    static void setNodeMapped(BaseNode preRoot, BaseNode curRoot, ProxyDao proxyDao, CommonInfo commonInfo) {
        if (preRoot == null && curRoot == null) {
            return;
        }
        if (preRoot == null) {
            curRoot.setMapping(true);
            curRoot.setRootUuid(curRoot.getUuid());
            return;
        }
        if (curRoot == null) {
            preRoot.setMapping(true);
            //删除情况
            TrackerInfo trackerInfo = getTrackerInfo(preRoot, proxyDao, commonInfo);
            if (trackerInfo != null) {
                preRoot.setRootUuid(trackerInfo.getRootUUID());
                preRoot.setVersion(trackerInfo.getVersion());
                return;
            }
            System.out.println("delete tracker info is null : " + preRoot.getProjectInfoLevel());
            return;
        }
        preRoot.setMapping(true);
        curRoot.setMapping(true);
        preRoot.setNextMappingBaseNode(curRoot);
        curRoot.setPreMappingBaseNode(preRoot);
        TrackerInfo trackerInfo = getTrackerInfo(preRoot, proxyDao, commonInfo);
        if (trackerInfo != null) {
            preRoot.setRootUuid(trackerInfo.getRootUUID());
            preRoot.setVersion(trackerInfo.getVersion());
            curRoot.setRootUuid(trackerInfo.getRootUUID());
            curRoot.setVersion(trackerInfo.getVersion() + 1);
            return;
        }
        //追溯不到 处理为ADD
        System.out.println("change tracker info is null : " + curRoot.getProjectInfoLevel());
        curRoot.setRootUuid(curRoot.getUuid());
        curRoot.setVersion(1);
        curRoot.setChangeStatus(BaseNode.ChangeStatus.ADD);
        if (curRoot instanceof StatementNode) {
            ((StatementNode)curRoot).setIsLogic(0);
        }

    }

    /**
     * 得到某个节点的追溯信息
     * @param baseNode 节点
     * @param commonInfo 公共信息
     * @param proxyDao  提供入库
     * @return tracker info
     */
    static TrackerInfo getTrackerInfo(BaseNode baseNode, ProxyDao proxyDao, CommonInfo commonInfo) {
        TrackerInfo trackerInfo = null;
        if (baseNode instanceof FileNode) {
            FileNode fileNode = (FileNode)baseNode;
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FILE,fileNode.getFilePath(),commonInfo.getRepoUuid(),commonInfo.getBranch());
        } else if (baseNode instanceof ClassNode) {
            ClassNode classNode = (ClassNode)baseNode;
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.CLASS,classNode.getFilePath(),classNode.getClassName(),commonInfo.getRepoUuid(),commonInfo.getBranch());
        } else if (baseNode instanceof FieldNode) {
            FieldNode fieldNode = (FieldNode)baseNode;
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.FIELD,fieldNode.getFilePath(),fieldNode.getClassName(),fieldNode.getSimpleName(),commonInfo.getRepoUuid(),commonInfo.getBranch());
        } else if (baseNode instanceof MethodNode) {
            MethodNode methodNode = (MethodNode)baseNode;
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.METHOD,methodNode.getFilePath(),methodNode.getClassName(),methodNode.getSignature(),commonInfo.getRepoUuid(),commonInfo.getBranch());
        } else if (baseNode instanceof StatementNode) {
            StatementNode statementNode = (StatementNode)baseNode;
            //将statement的methodUuid设成meta methodUuid
            if (statementNode.getParent() instanceof MethodNode) {
                MethodNode method = (MethodNode)statementNode.getParent();
                statementNode.setMethodUuid(method.getRootUuid());
            } else if (statementNode.getParent() instanceof StatementNode) {
                StatementNode statement = (StatementNode)statementNode.getParent();
                statementNode.setMethodUuid(statement.getMethodUuid());
            }
            trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.STATEMENT,statementNode.getMethodUuid(),statementNode.getBody());
        }
        return trackerInfo;
    }

    /**
     * 递归匹配以及设置节点
     * @param root r
     * @param proxyDao p
     * @param commonInfo c
     * @param status s
     */
    static void subTreeMappingRecursive(BaseNode root, CommonInfo commonInfo, ProxyDao proxyDao, BaseNode.ChangeStatus status) {
        Stack<BaseNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            BaseNode baseNode = stack.pop();
            baseNode.setChangeStatus(status);
            BaseNode pre = null;
            BaseNode cur = null;
            if (status.equals(BaseNode.ChangeStatus.ADD)) {
                cur = baseNode;
            }
            if (status.equals(BaseNode.ChangeStatus.DELETE)) {
                pre = baseNode;
            }
            baseNode.setChangeStatus(status);
            NodeMapping.setNodeMapped(pre, cur, proxyDao, commonInfo);
            pushChildrenIntoStack(baseNode, stack);
        }
    }

    /**
     * 将baseNode的孩子节点放入栈中
     * @param baseNode b
     * @param stack s
     */
    static void pushChildrenIntoStack(BaseNode baseNode, Stack<BaseNode> stack){
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
