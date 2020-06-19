package cn.edu.fudan.codetracker.domain.resultmap;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

//临时接口的result
@Getter
@Setter
public class TempMostInfo {
    private String name;
    private int quantity;
    private String uuid;
    private List<TempMostInfo> childInfos;
    private String committer;
    private String changeRelation;
    private double begin;
    private double height;
    private String description;
    private String filePath;
    private int lineBegin;
    private int lineEnd;
    private int LineHeight;


    public TempMostInfo() {

    }

}
