package cn.edu.fudan.codetracker.component;

/**
 * @author fancying
 */
public enum RestServiceEnum {
    /**
     * service name
     */
    ACCOUNT_SERVICE("account"),
    PROJECT_SERVICE("project"),
    CODE_SERVICE("code");

    private String name;
    RestServiceEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
