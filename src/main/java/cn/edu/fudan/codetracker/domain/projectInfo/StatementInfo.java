/**
 * @description:
 * @author: fancying
 * @create: 2019-05-26 21:22
 **/
package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class    StatementInfo implements Neo4jInformation{

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

    @Override
    public List<String> toNeo4jNode(String label) {
/*        List<String> cqlNodeList = new ArrayList<>();
        String cql = "CREATE (`" + uuid + "`:" + label + ":statement" + "{" +
                "body:\"" + body.replace("\"", "\\\"") +
                "\"})";
        cqlNodeList.add(cql);
        return cqlNodeList;*/
        return null;
    }

    public Map<String,String> StatToNeo4jNode(String label) {
        Map<String,String> cqlListPara = new HashMap<>();
        String cql = "CREATE (`" + uuid + "`:" + label + ":statement" + "{" +
                "uuid:\"" + uuid  +
                "\",body:{body},begin:" + begin +
                ",end:" + end  + "})";
        cqlListPara.put(cql,body);
        return cqlListPara;
    }

    @Override
    public List<String> toNeo4jRelation(String node1_label_name, String node2_label_name) {
        return null;
    }
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