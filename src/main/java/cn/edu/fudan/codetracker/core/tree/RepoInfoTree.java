package cn.edu.fudan.codetracker.core.tree;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import javafx.application.Application;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import sun.reflect.Reflection;

import java.lang.reflect.Field;
import java.util.*;

/**
 * description: 项目结构树 包含多种特定的语言树
 *
 * @author fancying
 * create: 2020-05-16 19:16
 **/
@Slf4j
public class RepoInfoTree {

    private Map<Language, BaseLanguageTree> repoTree;

    @Autowired
    private ApplicationContext applicationContext;

    @Setter
    private CommonInfo commonInfo;

    public RepoInfoTree(String filePath, CommonInfo commonInfo, List<String> relativePath, String repoUuid) {

    }

    public RepoInfoTree(String[] fileList, CommonInfo commonInfo, List<String> relativePath, String repoUuid) {

    }

    public RepoInfoTree(List<String> fileList, CommonInfo commonInfo, List<String> relativePath, String repoUuid) {
        construct(fileList, commonInfo, relativePath, repoUuid);
    }

    private void construct(List<String> fileList, CommonInfo commonInfo, List<String> relativePath, String repoUuid) {
        // 根据文件类型对文件进行分类
        Map<Language, Map<String,List<String>>> classifiedMap = classification(fileList, relativePath);
        // 根据分类结果 调用不同的parser
        Set<String> implementedClasses = applicationContext.getBeansOfType(BaseLanguageTree.class).keySet();
        String packageName = "cn.edu.fudan.codetracker.core.tree";
        for (String implementedClassName: implementedClasses) {
            try {
                String className = packageName + "." + implementedClassName;
                Class clazz = Class.forName(className);
                if (classifiedMap.keySet().contains(clazz.getDeclaredField("LANGUAGE"))) {
                    //使用对应树的实现类，初始化对应语言的树
                    List<String> files = classifiedMap.get(clazz.getDeclaredField("LANGUAGE")).get("files");
                    List<String> relativePaths = classifiedMap.get(clazz.getDeclaredField("LANGUAGE")).get("relativePaths");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.commonInfo = commonInfo ;
    }

    /**
     * 根据文件的后缀名将文件分类 并调用不同的parser
     */
    private Map<Language, Map<String,List<String>>> classification(List<String> fileList, List<String> relativePath) {
        Map<Language, Map<String,List<String>>> repoTree = new HashMap<>(4);
        try {
            Class clazz = Class.forName("com.cn.edu.fudan.codetracker.core.tree.Language");
            Language[] objects = (Language[])clazz.getEnumConstants();
            for (int i = 0; i < fileList.size() ; i++) {
                String file = fileList.get(i);
                String relativeFilePath = relativePath.get(i);
                for (Language language : objects) {
                    if (file.contains(language.getFilePostfix())) {
                        if (repoTree.keySet().contains(language)) {
                            repoTree.get(language).get("files").add(file);
                            repoTree.get(language).get("relativePaths").add(relativeFilePath);
                        } else {
                            Map<String,List<String>> map = new HashMap<>(2);
                            List<String> files = new ArrayList<>();
                            files.add(file);
                            map.put("files",files);
                            List<String> relativePaths = new ArrayList<>();
                            relativePaths.add(relativeFilePath);
                            map.put("relativePaths",relativePaths);
                            repoTree.put(language,map);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return repoTree;
    }


}