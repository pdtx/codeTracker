package cn.edu.fudan.codetracker.util;

import javancss.Javancss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavancssScaner {
    public static int scanFile(String repoPath) {
        int sum = 0;
        File projectDir = new File(repoPath);
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith(".java"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        for (String path: pathList) {
            File tempFile = new File(path);
            Javancss javancss = new Javancss(tempFile);
            int add = javancss.getNcss();
            sum += add;
        }
        return sum;
    }

    public static int scanOneFile(String path) {
        File tempFile = new File(path);
        Javancss javancss = new Javancss(tempFile);
        return javancss.getNcss();
    }

    public static void main(String[] args) {
        System.out.println(JavancssScaner.scanFile("/Users/tangyuan/Documents/Git/iec-wepm-develop"));
    }
}
