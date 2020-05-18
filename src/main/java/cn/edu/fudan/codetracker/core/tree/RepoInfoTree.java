package cn.edu.fudan.codetracker.core.tree;

import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 项目结构树 包含多种特定的语言树
 *
 * @author fancying
 * create: 2020-05-16 19:16
 **/
@Slf4j
public class RepoInfoTree {

    private Map<Language, BaseLanguageTree> repoTree;

    @Setter
    private CommonInfo commonInfo;

    public RepoInfoTree(String filePath, CommonInfo commonInfo) {

    }

    public RepoInfoTree(String[] fileList, CommonInfo commonInfo) {

    }

    public RepoInfoTree(List<String> fileList, CommonInfo commonInfo) {

    }

    private void construct(List<String> fileList, CommonInfo commonInfo) {
        // 根据文件类型对文件进行分类
        classification(fileList);
        // 根据分类结果 调用不同的parser

        this.commonInfo = commonInfo ;
    }

    /**
     * 根据文件的后缀名将文件分类 并调用不同的parser
     */
    private void classification(List<String> fileList) {
        Map<Language, List<String>> repoTree = new HashMap<>(4);
        for (String file : fileList) {

        }
    }


}