package cn.edu.fudan.codetracker.util;

/**
 * description:
 *
 * @author fancying
 * create: 2020-01-06 14:08
 **/
public final class FileFilter {
    /**
     * JPMS 模块
     */
    private static final String JPMS = "module-info.java";
    /**
     * true: 过滤
     * false： 不过滤
     */
    public  static boolean javaFilenameFilter(String path) {
        String[] strs = path.split("/");
        String str = strs[strs.length-1];
        return  !str.toLowerCase().endsWith(".java") ||
                path.toLowerCase().contains("/test/") ||
                path.toLowerCase().contains("/.mvn/") ||
                str.toLowerCase().endsWith("test.java") ||
                str.toLowerCase().endsWith("tests.java") ||
                str.toLowerCase().startsWith("test") ||
                str.toLowerCase().endsWith("enum.java") ||
                path.contains(JPMS);
    }
}