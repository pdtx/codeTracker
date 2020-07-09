/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 21:50
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackerInfo {


    /**
     * 根据具体情况 单独获取
     */
    private int version;
    private String changeRelation;
    private String rootUUID;
    private String description;
    private String lastChangeParent;

    public TrackerInfo() {

    }

    public TrackerInfo(String changeRelation) {
        this.changeRelation = changeRelation;
    }

    public TrackerInfo(String changeRelation, int version, String uuid) {
        this.changeRelation = changeRelation;
        this.version = version;
        this.rootUUID = uuid;
    }

    public TrackerInfo( int version, String uuid) {
        this.version = version;
        this.rootUUID = uuid;
        this.changeRelation = uuid;
    }

}