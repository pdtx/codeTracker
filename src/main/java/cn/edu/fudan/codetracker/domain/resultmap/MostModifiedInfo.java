/**
 * @description:
 * @author: fancying
 * @create: 2019-11-12 11:00
 **/
package cn.edu.fudan.codetracker.domain.resultmap;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MostModifiedInfo {
    private String uuid;
    private String moduleName;
    private String packageName;
    private String fileName;
    private String filePath;
    private String className;
    private String methodName;
    private int begin;
    private int end;
    private String changeRelation;
    private String description;

    private int version;

    MostModifiedInfo() {

    }

}