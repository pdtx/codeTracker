/**
 * @description:
 * @author: fancying
 * @create: 2019-09-19 10:40
 **/
package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.domain.ResponseBean;
import cn.edu.fudan.codetracker.service.ScanService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@EnableAutoConfiguration
public class RepoAnalyzerController {


    private ScanService scanService;

    @GetMapping(value = {"/scan"})
    public Object scan() {
        List<String> commitList = mockCommitList();
        scanService.firstScan("123", commitList,"master");
        return null;
    }
    /**
     * @param requestParam 包含： repoId、branch、duration
     */
    @PostMapping(value = {"/project"})
    public ResponseBean scan(@RequestBody JSONObject requestParam) {
        try {
            scanService.firstScan(requestParam.getString("repoId"), requestParam.getString("branch"), requestParam.getString("duration"));
            return new ResponseBean(200, "start scan", null);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
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