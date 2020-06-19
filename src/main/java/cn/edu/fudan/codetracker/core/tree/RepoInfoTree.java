package cn.edu.fudan.codetracker.core.tree;

import cn.edu.fudan.codetracker.component.ApplicationContextGetBeanHelper;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * description: 项目结构树 包含多种特定的语言树
 *
 * @author fancying
 * create: 2020-05-16 19:16
 **/
@Slf4j
@NoArgsConstructor
@Component
@Scope("prototype")
public class RepoInfoTree {

    @Getter
    private Map<Language, BaseLanguageTree> repoTree;

    @Setter
    private CommonInfo commonInfo;

    public RepoInfoTree(String filePath, CommonInfo commonInfo, String repoUuid) {

    }

    public RepoInfoTree(List<String> fileList, CommonInfo commonInfo, List<String> relativePath, String repoUuid) {
        // todo 适配 relativePath 与 fileList 实际上应该单独抽出来 新建一个类叫做 CLDIFF adapter
    }

    /**
     * @param fileList 路径地址
     */
    public RepoInfoTree(List<String> fileList, CommonInfo commonInfo, String repoUuid) {
        repoTree = new HashMap<>();
        construct(fileList, commonInfo, repoUuid);
    }

    private void construct(List<String> fileList, CommonInfo commonInfo, String repoUuid) {
        // 根据文件类型对文件进行分类
        Map<Language, List<String>> classifiedMap = classification(fileList);
        // 根据分类结果 调用不同的parser
        //ApplicationContextGetBeanHelper applicationContextGetBeanHelper = new ApplicationContextGetBeanHelper ();
        //applicationContextGetBeanHelper.setApplicationContext (applicationContext);

        for (Map.Entry<Language, List<String>> entry : classifiedMap.entrySet()) {
            Language language = entry.getKey();
            BaseLanguageTree baseLanguageTree = (BaseLanguageTree) ApplicationContextGetBeanHelper.getBean(language.getName());
            repoTree.put(language, constructTree(entry.getValue(), baseLanguageTree, repoUuid));
        }
        this.commonInfo = commonInfo ;
    }

    // 根据 parser 构造出
    private BaseLanguageTree constructTree(List<String> value, BaseLanguageTree baseLanguageTree, String repoUuid) {
        if (baseLanguageTree instanceof JavaTree) {
            return new JavaTree(value,repoUuid);
        } else if (baseLanguageTree instanceof CppTree) {
            return new CppTree(value,repoUuid);
        }
        return null;
    }

    /**
     * 仅用于文件分类
     */
    private Map<Language, List<String>> classification(List<String> fileList) {
        Map<Language, List<String>> result = new HashMap<>(4);
        for (String file : fileList) {
            Language language = getFileLanguage(file);
            if (language == null) {
                continue;
            }
            if (result.containsKey(language)) {
                result.get(language).add(file);
            } else {
                List<String> tmp =  new ArrayList<>();
                tmp.add(file);
                result.put(language, tmp);
            }
        }
        return result;
    }

    private Language getFileLanguage(String file) {
        for (Language language : Language.class.getEnumConstants()) {
            if (file.endsWith(language.getFilePostfix())) {
                return language;
            }
        }
//        log.warn("no suitable parser");
        return null;
    }

}