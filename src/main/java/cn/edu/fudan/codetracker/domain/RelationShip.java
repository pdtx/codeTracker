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
    MOVE("move");


    RelationShip (String name) {

    }
}
