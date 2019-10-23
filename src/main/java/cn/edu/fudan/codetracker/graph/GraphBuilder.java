/**
 * @description:
 * @author: fancying
 * @create: 2019-05-25 11:04
 **/
package cn.edu.fudan.codetracker.graph;

import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.util.DirExplorer;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

public class GraphBuilder implements AutoCloseable{

    private final Driver driver;
    private final String repoPath = "E:\\Lab\\project\\IssueTracker-Master";

    public GraphBuilder(String uri, String user, String password) {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    public Object getMess(String name) {

        JGitHelper jGitHelper = new JGitHelper(repoPath);

        Session session = driver.session();
        //bugSolvedInfo ConsumerRecord<String, String> consumerRecord
        String cql = "MATCH(n:`IssueTracker-Master`:method { signature:\"" + name + "\"})-[r:change]->\n" +
                    "(m:`IssueTracker-Master`:method{signature:\"" + name + "\"})  \n" +
                    "RETURN n.begin,n.end,type(r),m.begin,m.end,labels(n),labels(m)";
        System.out.println(cql);
        StatementResult statementResult = session.run(cql);
        List<Map<String,Object>> records = new ArrayList<>();
        Map<String,Object> basicInfo = new HashMap<>();
        List<Record> recordList = statementResult.list();
        basicInfo.put("total", recordList.size());
        basicInfo.put("method",name);
        records.add(basicInfo);
        Map recordMap ;
        boolean isFirst = true;
        for (Record record : recordList) {
            recordMap = record.asMap();
            List before = (List) recordMap.get("labels(n)");
            if (isFirst) {
                basicInfo.put("project",before.get(0));
                basicInfo.put("module",before.get(4));
                basicInfo.put("package",before.get(2));
                //basicInfo.put("file","KafkaConsumerService");
                basicInfo.put("class",before.get(3));
                isFirst = false;
            }
            Map<String,Object> changeInfo = new HashMap<>();
            Map<String,Object> changeDetail1 = new HashMap<>();
            Map<String,Object> changeDetail2 = new HashMap<>();
            String beforeCommit =  before.get(6).toString();
            if (beforeCommit.length() != 40)
                beforeCommit = before.get(5).toString();

            List after = (List) recordMap.get("labels(m)");
            String afterCommit =  after.get(6).toString();
            if (afterCommit.length() != 40)
                afterCommit = after.get(5).toString();

/*            InternalNode node = (InternalNode) recordMap.get("n");

            Map<String,Object> n = (HashMap)(node.asValue().asMap().get("properties"));
            Map<String,Object> m = (HashMap)recordMap.get("m");*/

            // 取代码 以及commit 信息
            setInfo(jGitHelper,changeDetail1,beforeCommit,
                    (String)before.get(3),((Long)recordMap.get("n.begin")).intValue(),((Long)recordMap.get("n.end")).intValue(),before.get(4).toString());
            setInfo(jGitHelper,changeDetail2,afterCommit,
                    (String)before.get(3),((Long)recordMap.get("m.begin")).intValue(),((Long)recordMap.get("m.end")).intValue(),before.get(4).toString());
            changeInfo.put("before",changeDetail1);
            changeInfo.put("after",changeDetail2);
            records.add(changeInfo);
        }

        return records;
    }

    private String getContent(String className, int begin, int end, String moduleName) {
        String path = findJavaFile(new File(repoPath),className, moduleName);
        int line = 1;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileReader in = new FileReader(new File(path));
            LineNumberReader reader = new LineNumberReader(in);
            while (line < begin) {
                reader.readLine();
                line++;
            }

            while (line <= end) {
                stringBuilder.append(reader.readLine());
                line++;
            }

            reader.close();
            in.close();
        }catch (Exception e) {
            e.printStackTrace();
        }


        return stringBuilder.toString();
    }

    private void setInfo(JGitHelper jGitHelper, Map<String, Object> changeDetail, String commit,String name,int begin,int end,String moduleName) {
        changeDetail.put("commit",commit);
        changeDetail.put("committer",jGitHelper.getAuthorName(commit));
        changeDetail.put("message",jGitHelper.getMess(commit));
        changeDetail.put("time",jGitHelper.getCommitTime(commit));
        jGitHelper.checkout(commit);
        String content = getContent(name,begin,end,moduleName);
        changeDetail.put("content",content);
    }

    private String findJavaFile(File projectDir, String className,String moduleName) {
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith(className + ".java"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);

        for (String s : pathList) {
            if (s.contains(moduleName))
                return s;
        }

        return pathList.get(0);
    }

    public Object getMess() {
        List<Map<String,Object>> records = new ArrayList<>();
        Map<String,Object> basicInfo = new HashMap<>();
        basicInfo.put("total",4);
        basicInfo.put("project","issueTracker");
        basicInfo.put("module","recommendation");
        basicInfo.put("package","cn.edu.fudan.bug_recommendation.service.impl");
        //basicInfo.put("file","KafkaConsumerService");
        basicInfo.put("class","KafkaConsumerService");
        basicInfo.put("method","bugSolvedInfo ConsumerRecord<String, String> consumerRecord");
        records.add(basicInfo);

        Map<String,Object> changeInfo = new HashMap<>();
        Map<String,Object> changeDetail1 = new HashMap<>();
        changeDetail1.put("commit","e99455dd2c0e2e76aae2b3b174c2d407107fff87");
        changeDetail1.put("committer","SouthStreet");
        changeDetail1.put("message","add");
        changeDetail1.put("time","2019-08-22 06:45:07");
        changeDetail1.put("content","    @KafkaListener(topics = {\"solvedBug\"})\n" +
                "    public void bugSolvedInfo(ConsumerRecord<String,String> consumerRecord){\n" +
                "            String msg = consumerRecord.value();\n" +
                "            System.out.println(\"receive message from topic -> \" + consumerRecord.topic() + \" : \" + msg);\n" +
                "            List<Recommendation> list = JSONObject.parseArray(msg, Recommendation.class);\n" +
                "            if (list != null) {\n" +
                "                for (Recommendation info : list) {\n" +
                "                    completeReco.completeCode(info);\n" +
                "                    String repoName = getCode.getRepoName(info.getRepoid());\n" +
                "                    String fileName = getCode.getFilePath(info.getLocation());\n" +
                "                    System.out.println(\"repoName: \" + repoName);\n" +
                "                    if(repoName!=null){\n" +
                "                        System.out.println(test);\n" +
                "                        recommendationService.addBugRecommendation(info);\n" +
                "                    }\n" +
                "\n" +
                "                }\n" +
                "            }\n" +
                "    }");

        Map<String,Object> changeDetail2 = new HashMap<>();
        changeDetail2.put("commit","3044ac547b4351d2a9024d42ae1b248bf7ae72fb");
        changeDetail2.put("committer","monity");
        changeDetail2.put("message","delete");
        changeDetail2.put("time","2019-08-23 11:12:50");
        changeDetail2.put("content","    @KafkaListener(topics = {\"solvedBug\"})\n" +
                "    public void bugSolvedInfo(ConsumerRecord<String,String> consumerRecord){\n" +
                "            String msg = consumerRecord.value();\n" +
                "            System.out.println(\"receive message from topic -> \" + consumerRecord.topic() + \" : \" + msg);\n" +
                "            List<Recommendation> list = JSONObject.parseArray(msg, Recommendation.class);\n" +
                "            if (list != null) {\n" +
                "                for (Recommendation info : list) {\n" +
                "                    completeReco.completeCode(info);\n" +
                "                    String repoName = getCode.getRepoName(info.getRepoid());\n" +
                "                    String fileName = getCode.getFilePath(info.getLocation());\n" +
                "                    info.setReponame(repoName);\n" +
                "                    info.setFilename(fileName);\n" +
                "                    System.out.println(\"repoName: \" + repoName);\n" +
                "//                JSONObject json = analyzeDiffFile.getDiffRange(newInfo.getLocation(),newInfo.getNext_commitid(),newInfo.getCurr_commitid(),newInfo.getBug_lines());\n" +
                "//                if(json.getInteger(\"nextstart_line\")!=0){\n" +
                "//                    newInfo.setStart_line(json.getInteger(\"start_line\"));\n" +
                "//                    newInfo.setEnd_line(json.getInteger(\"end_line\"));\n" +
                "//                    newInfo.setNextstart_line(json.getInteger(\"nextstart_line\"));\n" +
                "//                    newInfo.setNextend_line(json.getInteger(\"nextend_line\"));\n" +
                "//                    newInfo.setDescription(json.getString(\"description\"));\n" +
                "//                }\n" +
                "                    if(repoName!=null){\n" +
                "                        recommendationService.addBugRecommendation(info);\n" +
                "                    }\n" +
                "\n" +
                "                }\n" +
                "            }\n" +
                "    }");
        changeInfo.put("before",changeDetail1);
        changeInfo.put("after",changeDetail2);
        records.add(changeInfo);

        return  records;
    }

    public void executeBatchCql(List<String> cqlList) {
        //Driver driver = getNeo4jDriver();
        Session session = driver.session();
        for (String cql : cqlList) {
            try {
                session.run(cql);
            }catch (Exception e) {
                System.out.println("start");
                System.out.println(cql);
                System.out.println("end");
                e.printStackTrace();
            }

        }
        session.close();
       // driver.close();
    }

    public void executeBatchCqlStat(Map<String,String> cqlListPara) {
        ///Driver driver = getNeo4jDriver();
        Session session = driver.session();
        for (Map.Entry<String, String > cql : cqlListPara.entrySet()) {
            try {
                session.run(cql.getKey(),parameters("body", cql.getValue()));
                //session.run("",parameters(cqlListPara));
            }catch (Exception e) {
                System.out.println("START");
                System.out.println(cql);
                System.out.println("END");
                e.printStackTrace();
            }
        }
        session.close();
       // driver.close();


/*        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "CREATE (a:Greeting) " +
                                    "SET a.message = $message " +
                                    "RETURN a.message + ', from node ' + id(a)",
                            parameters( "message", message ) );
                    return result.single().get( 0 ).asString();
                }
            } );
            System.out.println( greeting );
        }*/
    }


    //判断package 是否存在

/*    private Driver getNeo4jDriver() {
        return GraphDatabase.driver( "bolt://10.141.221.84:7687", AuthTokens.basic( "neo4j", "1" ) );
    }*/

    @Override
    public void close() throws Exception {
        driver.close();
    }
}