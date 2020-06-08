package cn.edu.fudan.codetracker.domain;

/**
 * java 树上的节点和层级
 * @author fancying
 */
public enum ProjectInfoLevel {
    /**
     * module
     */
    MODULE("module",1),
    /**
     * package
     */
    PACKAGE("package",2),
    /**
     * file
     */
    FILE("file",3),
    /**
     * class
     */
    CLASS("class",4),
    /**
     * field
     */
    FIELD("field",5),
    /**
     * method
     */
    METHOD("method",5),
    /**
     *
     */
    STATEMENT("statement",6);

    ProjectInfoLevel(String name,int level) {
        this.name = name;
        this.level = level;
    }

    private String name;
    private int level;

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}
