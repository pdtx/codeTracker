package cn.edu.fudan.codetracker.domain.projectInfo;

import java.util.List;

public interface Neo4jInformation {
    public List<String> toNeo4jNode(String label);

    public List<String> toNeo4jRelation(String node1_label_name, String node2_label_name);
}
