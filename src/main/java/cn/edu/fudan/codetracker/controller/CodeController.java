/**
 * @description:
 * @author: fancying
 * @create: 2019-09-19 10:40
 **/
package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.graph.GraphBuilder;
import cn.edu.fudan.codetracker.service.ScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class CodeController {


    private ScanService scanService;




    @GetMapping(value = {"/code"})
    public Object getDetail(@RequestParam("methodName") String name) {

        final String uri = "bolt://10.141.221.84:7687";

        final String user = "neo4j";

        final String password = "1";

         GraphBuilder graphBuilder = new GraphBuilder(uri, user, password);

        return graphBuilder.getMess(name);
    }

    @GetMapping(value = {"/scan"})
    public Object scan() {
        List<String> commitList = mockCommitList();
        scanService.firstScan("123", commitList,"master");
        return null;
    }

    @GetMapping(value = {"/repo/{repoId}/{module}/{package}/{class}"})
    public Object getMethodHistory(@PathVariable("repoId")String repoId,@PathVariable("module") String moduleName,@PathVariable("package") String packageName,@PathVariable("class") String className, @RequestParam("signature") String signature) {
        return scanService.getMethodHistory(repoId, moduleName, packageName, className, signature);
    }

    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }


    private static List<String> mockCommitList() {
        List<String> list = new ArrayList<>();


/*        list.add("44a0b67ba8bcdfef6251030367a7502886d9fbb3");
        list.add("881ed977bd0c63e1e84b5b46e8bb1b0fb00ccd93");
        list.add("ef6cc9a15f1fb7c87bf1e428011946d80b14abdb");
        list.add("3addb10427974a17b10bbbbef721328bd2a90403");
        list.add("8ce3f93f56b0c80b2b3253c27757f01a3b2840fc");
        list.add("b08b88410619cd018b2884aa763b0762b94c5545");
        list.add("b00a2b54f55cb999a09143f6c2e11353a9b3cc8f");
        list.add("58eb9a9e87f224c551d5b72cb06176c9419e7f67");
        list.add("a6fb29ea324e87bead0471096dac9cd7cbb91a41");
        list.add("0270d3ed338dde4b88aaaa698d7e5ba12c9a3caa");
        list.add("f868d8f411db57b33be905c686d55c25820ea298");
        list.add("8b6293deb05cd6c0cff93fb6beff5faf878e4507");
        list.add("581b3a00a9bb2240e46cf432d5a4cf39885fb3e9");*/


        list.add("e99455dd2c0e2e76aae2b3b174c2d407107fff87");
        list.add("9bfd231ccc544c0fd945fcb352bcdffd9344a6f2");
        list.add("3044ac547b4351d2a9024d42ae1b248bf7ae72fb");
        list.add("7612a80f6fed7400937158176e098ccad2040705");
        list.add("4645cd23ea02fac2124bc78d7883cad0b1b2afa3");
        list.add("661ef719085a3f12413197b8f6b86d61bf18f51a");
        list.add("c8becfc82fbb5c981c4b62462725fabcb3075a6d");
        list.add("831837e67e0dca20c42213a43fe137346b8234d5");
        list.add("2e27dc8b1154ae40dc46e032b724004701cb95e7");
        list.add("e42c1f939a318c6b7d2b06b900bdf48af43d6997");
        list.add("494c76d45ad2c0f223168b924f3a0b7c49dee498");
        list.add("88feb052613d941f3f67b0f0d46244b00375b704");
        list.add("c933b00e25b70df9777e64652f44ab218cd57b69");
        list.add("47e22a6a8a05dd2c5fb526db5faea55357a66951");
        list.add("6d2bfe75965e05ef18cc481bac66889995942ec0");
        list.add("f6a1b4ef9ebaab4e0c8cfefb86ba7b39aa59e970");

        return list;
    }
}