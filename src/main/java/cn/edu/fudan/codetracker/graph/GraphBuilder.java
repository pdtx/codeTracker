/**
 * @description:
 * @author: fancying
 * @create: 2019-05-25 11:04
 **/
package cn.edu.fudan.codetracker.graph;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;

public class GraphBuilder implements AutoCloseable{

    private final Driver driver;

    public GraphBuilder(String uri, String user, String password) {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    public Object getMess(String name) {
        Session session = driver.session();
        //bugSolvedInfo ConsumerRecord<String, String> consumerRecord
        String cql = "MATCH(n:`IssueTracker-Master`:method { signature:\"" + name + "\"})-[r:change]->\n" +
                    "(m:`IssueTracker-Master`:method{signature:\"" + name + "\"})  \n" +
                    "RETURN n,type(r),m,labels(n),labels(m)";
        System.out.println(cql);
        StatementResult statementResult = session.run(cql);
        List<Map<String,Object>> records = new ArrayList<>();

        Map recordMap ;
        while (statementResult.hasNext()) {
            Record record = statementResult.next();
            recordMap = record.asMap();
            Map recordCopy  = new HashMap(recordMap);
            recordCopy.remove("n");
            recordCopy.remove("m");
            recordCopy.remove("type(r)");
            records.add(recordCopy);
        }
        Map<String,Object> info = new HashMap<>();
        info.put("total",records.size());
        records.add(info);

        return records;
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