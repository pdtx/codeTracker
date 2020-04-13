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
    public  static boolean filenameFilter(String path) {
        String[] strs = path.split("/");
        String str = strs[strs.length-1];
        boolean isContinue = !str.toLowerCase().endsWith(".java") ||
                path.toLowerCase().contains("/test/") ||
                str.toLowerCase().endsWith("test.java") ||
                str.toLowerCase().endsWith("tests.java") ||
                str.toLowerCase().startsWith("test") ||
                str.toLowerCase().endsWith("enum.java") ||
                path.contains(JPMS);
        return isContinue;
    }
}