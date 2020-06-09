package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.core.tree.JavaTree;
import cn.edu.fudan.codetracker.dao.ProxyDao;
import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.FileNode;
import cn.edu.fudan.codetracker.util.FileFilter;

import java.util.*;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-09 22:22
 **/
public class FileRenameHandler implements PublicConstants {

    public static void dealWithRename(List<String> renameList, JavaTree preRepoInfo, JavaTree curRepoInfo, CommonInfo commonInfo, ProxyDao proxyDao) {

        Set<String> preRenameSet = new HashSet<>();
        Set<String> curRenameSet = new HashSet<>();
        for (String str: renameList) {
            String[] paths = str.split(DELIMITER_RENAME);
            preRenameSet.add(paths[0]);
            curRenameSet.add(paths[1]);
        }
        int renameInitialCapacity = curRenameSet.size() << 1;
        Map<String,FileNode> preRenameMap = new HashMap<>(renameInitialCapacity);
        Map<String,FileNode> curRenameMap = new HashMap<>(renameInitialCapacity);
        preRepoInfo.getFileInfos().stream().
                filter(f -> preRenameSet.contains(f.getFilePath())).
                forEach(f -> preRenameMap.put(f.getFilePath(), f));
        curRepoInfo.getFileInfos().stream().
                filter(f -> curRenameSet.contains(f.getFilePath())).
                forEach(f -> curRenameMap.put(f.getFilePath(),f));


        for (String str: renameList) {
            String[] paths = str.split(DELIMITER_RENAME);
            if (FileFilter.javaFilenameFilter(paths[0]) || FileFilter.javaFilenameFilter(paths[1])) {
                continue;
            }
            FileNode preRenameRoot = preRenameMap.get(paths[0]);
            FileNode curRenameRoot = curRenameMap.get(paths[1]);
            curRenameRoot.setChangeStatus(BaseNode.ChangeStatus.SELF_CHANGE);
            NodeMapping.setNodeMapped(preRenameRoot,curRenameRoot,proxyDao,commonInfo);
        }

    }
}