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
 * description: 处理文件重命名的情况
 *
 * @author fancying
 * create: 2020-06-09 22:22
 **/
public class FileRenameHandler implements PublicConstants {

    public static void dealWithRename(List<String> renameList, JavaTree preRepoInfo, JavaTree curRepoInfo, CommonInfo commonInfo, ProxyDao proxyDao) {

        // 预处理

        // key: prePath value:curPath
        Map<String, String> renameMap = new LinkedHashMap<>();
        for (String str: renameList) {
            String[] paths = str.split(DELIMITER_RENAME);
            renameMap.put(paths[0], paths[1]);
        }
        int renameInitialCapacity = renameMap.size() << 1;
        Map<String, FileNode> preRenameMap = new HashMap<>(renameInitialCapacity);
        Map<String, FileNode> curRenameMap = new HashMap<>(renameInitialCapacity);
        preRepoInfo.getFileInfos().stream().
                filter(f -> renameMap.keySet().contains(f.getFilePath())).
                forEach(f -> preRenameMap.put(f.getFilePath(), f));
        curRepoInfo.getFileInfos().stream().
                filter(f -> renameMap.values().contains(f.getFilePath())).
                forEach(f -> curRenameMap.put(f.getFilePath(),f));

        // 遍历rename的map
        for (Map.Entry<String, String> entry : renameMap.entrySet()) {

        }


    }
}