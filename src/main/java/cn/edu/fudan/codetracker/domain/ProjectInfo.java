package cn.edu.fudan.codetracker.domain;

public enum ProjectInfo {
    /**
     * module
     */
    MODULE("module"),
    /**
     * package
     */
    PACKAGE("package"),
    /**
     * file
     */
    FILE("file"),
    /**
     * class
     */
    CLASS("class"),
    /**
     * field
     */
    FIELD("field"),
    /**
     * method
     */
    METHOD("method");

    ProjectInfo(String name) {

    }
}
