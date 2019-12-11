package cn.edu.fudan.codetracker.domain;

public enum RelationShip {
    /**
     * change
     */
    CHANGE("change"),
    /**
     * add
     */
    ADD("add"),
    /**
     * delete
     */
    DELETE("delete"),
    /**
     * move
     */
    MOVE("move"),
    /**
     * self change
     */
    SELF_CHANGE("selfChange");


    RelationShip (String name) {

    }
}
