package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

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
     */
    void subTreeMapping(BaseNode preRoot, BaseNode curRoot, CommonInfo commonInfo, ProxyDao proxyDao);

    /**
     * 设置两个节点的映射状态
     * @param preRoot 前一个版本的根结点
     * @param curRoot 当前版本的根节点
     */
    static void setNodeMapped(BaseNode preRoot, BaseNode curRoot, ProxyDao proxyDao, CommonInfo commonInfo) {
        if (preRoot == null && curRoot == null) {
            return;
        } else if (preRoot == null) {
            curRoot.setMapping(true);
            curRoot.setRootUuid(curRoot.getUuid());
        } else if (curRoot == null) {
            preRoot.setMapping(true);
            //删除情况
            TrackerInfo trackerInfo = getTrackerInfo(preRoot,proxyDao,commonInfo);
            if (trackerInfo != null) {
                preRoot.setRootUuid(trackerInfo.getRootUUID());
                preRoot.setVersion(trackerInfo.getVersion());
            }
//            else {
//                System.out.println("delete null : " + preRoot.getProjectInfoLevel());
//            }
        } else {
            preRoot.setMapping(true);
            curRoot.setMapping(true);
            preRoot.setNextMappingBaseNode(curRoot);
            curRoot.setPreMappingBaseNode(preRoot);
            TrackerInfo trackerInfo = getTrackerInfo(preRoot,proxyDao,commonInfo);
            if (trackerInfo != null) {
                preRoot.setRootUuid(trackerInfo.getRootUUID());
                preRoot.setVersion(trackerInfo.getVersion());
                curRoot.setRootUuid(trackerInfo.getRootUUID());
                curRoot.setVersion(trackerInfo.getVersion()+1);
            } else {
//                System.out.println("change null : " + preRoot.getProjectInfoLevel());
                //追溯不到 处理为ADD
                curRoot.setRootUuid(curRoot.getUuid());
                curRoot.setVersion(1);
                curRoot.setChangeStatus(BaseNode.ChangeStatus.ADD);
                if (curRoot instanceof StatementNode) {
                    StatementNode statementNode = (StatementNode)curRoot;
                    statementNode.setIsLogic(0);
                }
            }
        }
    }

    static TrackerInfo getTrackerInfo(BaseNode baseNode,ProxyDao proxyDao,CommonInfo commonInfo) {
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

}
