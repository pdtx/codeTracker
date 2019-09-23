/**
 * @description:
 * @author: fancying
 * @create: 2019-09-19 10:40
 **/
package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.graph.GraphBuilder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class CodeController {




    @GetMapping(value = {"/code"})
    public Object getDetail(@RequestParam("methodName") String name) {

        final String uri = "bolt://10.141.221.84:7687";

        final String user = "neo4j";

        final String password = "1";

         GraphBuilder graphBuilder = new GraphBuilder(uri, user, password);


        return graphBuilder.getMess(name);
    }



}