/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 21:22
 **/
package cn.edu.fudan.codetracker.domain.projectinfo;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementInfo extends BaseInfo{

    private String uuid;
    private String body;
    private int begin;
    private int end;

    public StatementInfo(String uuid, String body, int begin, int end) {
        this.uuid = uuid;
        this.body = body;
        this.begin = begin;
        this.end = end;
    }

    public StatementInfo(BaseInfo baseInfo, List<StatementInfo> children, BaseInfo parent,String uuid, String body, int begin, int end) {
        super(baseInfo);
        super.setParent(parent);
        super.setChildren(children);
        super.setProjectInfoLevel(ProjectInfoLevel.STATEMENT);
        this.uuid = uuid;
        this.body = body;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }


    /**
     * getter and setter
     */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public StatementInfo(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}