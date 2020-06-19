package cn.edu.fudan.codetracker.util;

import lombok.SneakyThrows;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * description: 对java pom 文件的解析
 *
 * @author fancying
 * create: 2020-06-18 15:45
 **/
public class PomAnalysisUtil {

    /**
     * 得到某个项目地址下 所有pom中的 group id
     * @param repoPath 项目绝对地址
     * @return List<String>
     */
    public static Set<String> getAllGroupId(String repoPath) {
        Set<String> result = new HashSet<>(4);
        new DirExplorer(((level, path, file) -> path.endsWith("pom.xml")),
                (level, path, file) -> result.add(getGroupId(file.getAbsolutePath()))).explore(new File(repoPath));
        return result;
    }

    /**
     * 得到某个pom中的 group id
     * @param pomPath pom.xml 的绝对地址
     * @return String
     */
    @SneakyThrows
    public static String getGroupId(String pomPath) {
        //pom 为 pom.xml 路径
        FileInputStream fis = new FileInputStream(new File(pomPath));
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(fis);
        return model.getGroupId();
    }

}