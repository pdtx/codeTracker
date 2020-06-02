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
    JAVA(".java","java"),
    /**
     * c++
     */
    CPP(".cpp","c++");

    Language(String filePostfix, String name) {
        this.filePostfix = filePostfix;
        this.name = name;
    }
    private final String name;
    private final String filePostfix;

}
