package cn.edu.fudan.codetracker.domain.projectinfo;

public enum RelationShip {

    CHANGE("change"),
    ADD("add"),
    DELETE("delete"),
    MOVE("move"),
    NOTCHANGE("notChange");


    private RelationShip (String name) {

    }
}
