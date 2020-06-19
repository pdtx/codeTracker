package cn.edu.fudan.codetracker.domain.projectinfo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * description: 方法调用关系
 *
 * @author fancying
 * create: 2020-06-18 17:28
 **/
@Data
@AllArgsConstructor
public class MethodCallRelationship {

    private String packageName;
    private String className;
    private String signature;



}