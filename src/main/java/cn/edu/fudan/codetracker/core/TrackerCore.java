package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.core.tree.JavaTree;
import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.FileFilter;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.data.util.CastUtils.cast;

/**
 * description: 每个项目一个实例
 *
 * @author fancying
 * create: 2020-03-20 19:32
 **/
@Data
@Slf4j
@Component
public class TrackerCore {

    private TrackerCore() {}

    public static TrackerCore getInstance() {
        return CoreGeneratorHolder.TRACKER_CORE;
    }

    private static final class CoreGeneratorHolder{
        private static final TrackerCore TRACKER_CORE = new TrackerCore();
    }

    /**
     *  每个 线程/repo 单独持有一个 PROJECT_STRUCTURE_TREE： 含有多个 版本树
     */
    private static final ThreadLocal<Map<String, BaseNode>> PROJECT_STRUCTURE_TREE = new ThreadLocal<>();

    /**
     *  CommonInfo 存储的是某个版本的项目结构树上所有节点共享的信息，描述的是匹配的两个版本的信息
     *   随着每一个匹配内容会改变
     */
    private static final ThreadLocal<CommonInfo> COMMON_INFO_THREAD_LOCAL = new ThreadLocal<>();


    private static ProxyDao proxyDao;
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static AddHandler addHandler = AddHandler.getInstance();
    private static DeleteHandler deleteHandler = DeleteHandler.getInstance();
    private static LogicalChangedHandler logicalChangedHandler = LogicalChangedHandler.getInstance();
    private static PhysicalChangedHandler physicalChangedHandler = PhysicalChangedHandler.getInstance();


    public static void mappingModule() {

    }

    public static void mapping(JavaTree preRepoInfo, JavaTree curRepoInfo, CommonInfo preCommonInfo, String repoUuid, String branch, Map<String, List<String>> map, Map<String,Map<String,String>> logicalChangedFileMap, String outputPath, String preCommit) {
        Set<PackageNode> packageNodeSet;
        if (preRepoInfo == null && curRepoInfo == null) {
            return;
        } else if (preRepoInfo == null) {
            packageNodeSet = new HashSet<>(curRepoInfo.getPackageInfos());
            mappingPackageNode(packageNodeSet,repoUuid,branch);
            for (FileNode fileNode : curRepoInfo.getFileInfos()) {
                if (FileFilter.javaFilenameFilter(fileNode.getFilePath())) {
                    continue;
                }
                addHandler.subTreeMapping(null,fileNode,preCommonInfo,proxyDao);
            }
        } else if (curRepoInfo == null) {
            packageNodeSet = new HashSet<>(preRepoInfo.getPackageInfos());
            mappingPackageNode(packageNodeSet,repoUuid,branch);
            for (FileNode fileNode : preRepoInfo.getFileInfos()) {
                if (FileFilter.javaFilenameFilter(fileNode.getFilePath())) {
                    continue;
                }
                deleteHandler.subTreeMapping(fileNode,null,preCommonInfo,proxyDao);
            }
        } else {
            //处理packageNode
            packageNodeSet = new HashSet<>(curRepoInfo.getPackageInfos());
            packageNodeSet.addAll(preRepoInfo.getPackageInfos());
            mappingPackageNode(packageNodeSet,repoUuid,branch);

            //判断fileNode属于add、delete、change
            Set<String> addSet = new HashSet<>(map.get("ADD"));
            Set<String> deleteSet = new HashSet<>(map.get("DELETE"));
            Set<String> changeSet = new HashSet<>(map.get("CHANGE"));
            Set<String> preRenameSet = new HashSet<>();
            Set<String> curRenameSet = new HashSet<>();
            for (String str: map.get("RENAME")) {
                String[] paths = str.split(":");
                preRenameSet.add(paths[0]);
                curRenameSet.add(paths[1]);
            }
            Map<String,FileNode> preMap = new HashMap<>();
            Map<String,FileNode> curMap = new HashMap<>();
            Map<String,FileNode> preRenameMap = new HashMap<>();
            Map<String,FileNode> curRenameMap = new HashMap<>();

            for (FileNode fileNode : preRepoInfo.getFileInfos()) {
                if (FileFilter.javaFilenameFilter(fileNode.getFilePath())) {
                    continue;
                }
                if (deleteSet.contains(fileNode.getFilePath())) {
                    deleteHandler.subTreeMapping(fileNode, null,preCommonInfo,proxyDao);
                }
                if (changeSet.contains(fileNode.getFilePath())) {
                    preMap.put(fileNode.getFilePath(),fileNode);
                }
                if (preRenameSet.contains(fileNode.getFilePath())) {
                    preRenameMap.put(fileNode.getFilePath(),fileNode);
                }
            }
            for (FileNode fileNode : curRepoInfo.getFileInfos()) {
                if (FileFilter.javaFilenameFilter(fileNode.getFilePath())) {
                    continue;
                }
                if (addSet.contains(fileNode.getFilePath())) {
                    addHandler.subTreeMapping(null,fileNode,preCommonInfo,proxyDao);
                }
                if (changeSet.contains(fileNode.getFilePath())) {
                    curMap.put(fileNode.getFilePath(),fileNode);
                }
                if (curRenameSet.contains(fileNode.getFilePath())) {
                    curRenameMap.put(fileNode.getFilePath(),fileNode);
                }
            }

            Map<String,String> logicalFileMap = logicalChangedFileMap.get(preCommit);

            for (String path : changeSet) {
                if (FileFilter.javaFilenameFilter(path)) {
                    continue;
                }
                FileNode preRoot = preMap.get(path);
                FileNode curRoot = curMap.get(path);
                //判断文件是否有逻辑修改
                if(logicalFileMap.keySet().contains(path)) {
                    String diffPath = outputPath + (IS_WINDOWS ? "\\" : "/") + logicalFileMap.get(path);
                    logicalChangedHandler.setDiffPath(diffPath);
                    logicalChangedHandler.subTreeMapping(preRoot,curRoot,preCommonInfo,proxyDao);
                } else {
                    physicalChangedHandler.subTreeMapping(preRoot,curRoot,preCommonInfo,proxyDao);
                }
            }
            //处理rename情况 待完善
            for (String str: map.get("RENAME")) {
                String[] paths = str.split(":");
                FileNode preRenameRoot = preRenameMap.get(paths[0]);
                FileNode curRenameRoot = curRenameMap.get(paths[1]);
                dealWithRename(preRenameRoot,curRenameRoot,proxyDao,preCommonInfo);
            }

        }

    }

    private static void dealWithRename(BaseNode preRoot, BaseNode curRoot, ProxyDao proxyDao, CommonInfo commonInfo) {
        curRoot.setChangeStatus(BaseNode.ChangeStatus.SELF_CHANGE);
        NodeMapping.setNodeMapped(preRoot,curRoot,proxyDao,commonInfo);
        //如何实现rename文件间的diff，待完善
    }

    private static void mappingPackageNode(Set<PackageNode> packageNodeSet, String repoUuid, String branch) {
        for (PackageNode packageNode : packageNodeSet) {
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.PACKAGE,packageNode.getModuleName(),packageNode.getPackageName(),repoUuid,branch);
            if (trackerInfo == null) {
                packageNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                packageNode.setRootUuid(packageNode.getUuid());
            } else {
                packageNode.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
                packageNode.setRootUuid(trackerInfo.getRootUUID());
                packageNode.setVersion(trackerInfo.getVersion()+1);
            }
            packageNode.setMapping(true);
        }
    }


    private static void changeMapping(PackageNode prePackageNode, PackageNode curPackageNode) {

    }

    private static BaseNode findSimilarNode(List<? extends BaseNode> nodeList, BaseNode target) {
        // FIXME
        return target;
    }

    @Autowired
    public void setProxyDao(ProxyDao proxyDao) {
        TrackerCore.proxyDao = proxyDao;
    }

    public static ThreadLocal<Map<String, BaseNode>> getProjectStructureTree() {
        return PROJECT_STRUCTURE_TREE;
    }

    public static ThreadLocal<CommonInfo> getCommonInfoThreadLocal() {
        return COMMON_INFO_THREAD_LOCAL;
    }

    public static void remove() {
        COMMON_INFO_THREAD_LOCAL.remove();
        PROJECT_STRUCTURE_TREE.remove();
    }


}