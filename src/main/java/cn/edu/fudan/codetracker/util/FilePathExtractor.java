package cn.edu.fudan.codetracker.util;

/**
 * @author chenyuan
 */
public class FilePathExtractor {
    /**
     * @param repoPath repo地址 即基础服务返回地址
     * @param path 文件绝对路径
     * @return
     */
    public static String extractFilePath(String repoPath, String path) {
        return path.replace('\\','/').replaceFirst(repoPath.replace('\\','/') + '/',"");
    }
}
