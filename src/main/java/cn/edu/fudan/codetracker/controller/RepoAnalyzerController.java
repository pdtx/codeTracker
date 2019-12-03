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

@RestController
@EnableAutoConfiguration
public class RepoAnalyzerController {


    private ScanService scanService;

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

    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }


}