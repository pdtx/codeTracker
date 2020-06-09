package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.core.tree.JavaTree;
import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import cn.edu.fudan.codetracker.domain.diff.CldiffAdapter;
import cn.edu.fudan.codetracker.domain.diff.DiffInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.util.FileFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description: 每个项目一个实例
 *
 * @author fancying
 * create: 2020-03-20 19:32
 **/
@Data
@Slf4j
@Component
public class TrackerCore implements PublicConstants {

    private TrackerCore() {}

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
    private static AddHandler addHandler = AddHandler.getInstance();
    private static DeleteHandler deleteHandler = DeleteHandler.getInstance();
    private static LogicalChangedHandler logicalChangedHandler = LogicalChangedHandler.getInstance();
    private static PhysicalChangedHandler physicalChangedHandler = PhysicalChangedHandler.getInstance();


    public static void mappingModule() {

    }

    public static void mapping(JavaTree preRepoInfo, JavaTree curRepoInfo, CommonInfo preCommonInfo, String repoUuid, String branch, Map<String, List<String>> map, Map<String,Map<String,String>> logicalChangedFileMap, String outputPath, String preCommit) {
        if (preRepoInfo == null && curRepoInfo == null) {
            return;
        }
        // package 处理
        if (packageHandler(preRepoInfo, curRepoInfo, repoUuid, branch, preCommonInfo)){
            return;
        }

        Set<String> addSet = new HashSet<>(map.get(ADD));
        Set<String> deleteSet = new HashSet<>(map.get(DELETE));
        Set<String> changeSet = new HashSet<>(map.get(CHANGE));
        int mapInitialCapacity = changeSet.size() << 1;
        Map<String,FileNode> preChangedFileMap = new HashMap<>(mapInitialCapacity);
        Map<String,FileNode> curChangedFileMap = new HashMap<>(mapInitialCapacity);

        // fixme 之前已近过滤了 这里还有必要？ 过滤非java文件等无法解析的文件
        List<FileNode> preFileNodes = preRepoInfo.getFileInfos().stream().
                filter(f -> !FileFilter.javaFilenameFilter(f.getFilePath())).
                collect(Collectors.toList());
        List<FileNode> curFileNodes = preRepoInfo.getFileInfos().stream().
                filter(f -> !FileFilter.javaFilenameFilter(f.getFilePath())).
                collect(Collectors.toList());

        // 归类 add、delete、change，并处理add 、delete 情况
        preFileNodes.stream().
                filter(f -> deleteSet.contains(f.getFilePath())).
                forEach(f -> deleteHandler.subTreeMapping(f, null, preCommonInfo, proxyDao));
        preFileNodes.stream().
                filter(f -> changeSet.contains(f.getFilePath())).
                forEach(f -> preChangedFileMap.put(f.getFilePath(), f));

        curFileNodes.stream().
                filter(f -> addSet.contains(f.getFilePath())).
                forEach(f -> addHandler.subTreeMapping(null, f, preCommonInfo, proxyDao));
        curFileNodes.stream().
                filter(f -> changeSet.contains(f.getFilePath())).
                forEach(f -> curChangedFileMap.put(f.getFilePath(), f));

        // 处理逻辑上和物理上改变的情况
        Map<String,String> logicalFileMap = logicalChangedFileMap.get(preCommit);
        for (String path : changeSet) {
            if (FileFilter.javaFilenameFilter(path)) {
                continue;
            }
            FileNode preRoot = preChangedFileMap.get(path);
            FileNode curRoot = curChangedFileMap.get(path);
            //判断文件是否有逻辑修改
            if(logicalFileMap.keySet().contains(path)) {
                String diffPath = outputPath + (IS_WINDOWS ? "\\" : "/") + logicalFileMap.get(path);
                logicalChangedHandler.setMapThreadLocal(CldiffAdapter.extractFromDiff(diffPath));
                logicalChangedHandler.subTreeMapping(preRoot, curRoot, preCommonInfo, proxyDao);
                continue;
            }
            physicalChangedHandler.subTreeMapping(preRoot, curRoot, preCommonInfo, proxyDao);
        }

        // 处理rename情况
        FileRenameHandler.dealWithRename(map.get(RENAME), preRepoInfo, curRepoInfo,  preCommonInfo, proxyDao);

    }

    private static boolean packageHandler(JavaTree preRepoInfo, JavaTree curRepoInfo, String repoUuid, String branch, CommonInfo preCommonInfo) {
        Set<PackageNode> packageNodeSet;
        if (preRepoInfo == null) {
            packageNodeSet = new HashSet<>(curRepoInfo.getPackageInfos());
            mappingPackageNode(packageNodeSet, repoUuid, branch);
            curRepoInfo.getFileInfos().stream().
                    filter(f -> !FileFilter.javaFilenameFilter(f.getFilePath())).
                    forEach(f -> addHandler.subTreeMapping(null, f, preCommonInfo, proxyDao));
            return true;
        }
        if (curRepoInfo == null) {
            packageNodeSet = new HashSet<>(preRepoInfo.getPackageInfos());
            mappingPackageNode(packageNodeSet, repoUuid, branch);
            preRepoInfo.getFileInfos().stream().
                    filter(f -> !FileFilter.javaFilenameFilter(f.getFilePath())).
                    forEach(f -> deleteHandler.subTreeMapping(f,null, preCommonInfo, proxyDao));
            return true;
        }
        //处理packageNode
        packageNodeSet = new HashSet<>(curRepoInfo.getPackageInfos());
        packageNodeSet.addAll(preRepoInfo.getPackageInfos());
        mappingPackageNode(packageNodeSet, repoUuid, branch);
        return false;
    }

    /**
     * todo 没有考虑package删除的情况
     */
    private static void mappingPackageNode(Set<PackageNode> packageNodeSet, String repoUuid, String branch) {
        for (PackageNode packageNode : packageNodeSet) {
            TrackerInfo trackerInfo = proxyDao.getTrackerInfo(ProjectInfoLevel.PACKAGE, packageNode.getModuleName(), packageNode.getPackageName(), repoUuid, branch);
            if (trackerInfo == null) {
                packageNode.setChangeStatus(BaseNode.ChangeStatus.ADD);
                packageNode.setRootUuid(packageNode.getUuid());
            } else {
                packageNode.setChangeStatus(BaseNode.ChangeStatus.CHANGE);
                packageNode.setRootUuid(trackerInfo.getRootUUID());
                packageNode.setVersion(trackerInfo.getVersion() + 1);
            }
            packageNode.setMapping(true);
        }
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