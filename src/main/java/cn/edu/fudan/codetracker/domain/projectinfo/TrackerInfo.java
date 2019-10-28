/**
 * @description:
 * @author: fancying
 * @create: 2019-09-26 21:50
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

public class TrackerInfo {


    // 根据具体情况 单独获取
    private int version;
    private String changeRelation;
    private String rootUUID;

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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setRootUUID(String rootUUID) {
        this.rootUUID = rootUUID;
    }

    public String getChangeRelation() {
        return changeRelation;
    }

    public String getRootUUID() {
        return rootUUID;
    }

    public void setChangeRelation(String changeRelation) {
        this.changeRelation = changeRelation;
    }
}