package cn.edu.fudan.codetracker.core.tree;

import lombok.Getter;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-17 15:05
 **/
@Getter
public enum Language {

    /**
     * java
     */
    JAVA(".java"),
    /**
     * c++
     */
    CPP(".cpp");

    Language(String filePostfix) {
        this.filePostfix = filePostfix;
    }

    private String filePostfix;

}
