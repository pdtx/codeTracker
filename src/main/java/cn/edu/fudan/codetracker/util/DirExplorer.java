package cn.edu.fudan.codetracker.util;

import java.io.File;
import java.util.Objects;

/**
 * description: 遍历得到所有的文件
 * @author fancying
 * create: 2019-05-24 11:52
 **/
public class DirExplorer {

    public interface Filter {
        boolean filter(int level, String path, File file);
    }

    public interface FileHandler {
        void handle(int level, String path, File file);
    }

    private Filter filter;
    private FileHandler fileHandler;

    public DirExplorer(Filter filter, FileHandler fileHandler) {
        this.filter = filter;
        this.fileHandler = fileHandler;
    }

    public void explore(File root) {
        explore(0, "", root);
    }

    private void explore(int level, String path, File file) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                explore(level + 1, path + "/" + child.getName(), child);
            }
        } else {
            if (filter.filter(level, path, file)) {
                fileHandler.handle(level, path, file);
            }
        }
    }

    public void exploreDir(File root) {
        exploreDir(0, "", root);
    }

    private void exploreDir(int level, String path, File file) {
        if (file.isDirectory()) {
            if (filter.filter(level, path, file)) {
                fileHandler.handle(level, path, file);
            }

            for (File child : Objects.requireNonNull(file.listFiles())) {
                exploreDir(level + 1, path + "/" + child.getName(), child);
            }
        }

    }
}