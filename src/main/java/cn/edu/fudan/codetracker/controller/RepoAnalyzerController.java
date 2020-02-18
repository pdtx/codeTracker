/**
 * @description:
 * @author: fancying
 * @create: 2019-09-19 10:40
 **/
package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.domain.ResponseBean;
import cn.edu.fudan.codetracker.service.ScanService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
            String status = scanService.getScanStatus(requestParam.getString("repoId"), requestParam.getString("branch"));
            if (status == null || !status.equals("scanning")) {
                scanService.firstScan(requestParam.getString("repoId"), requestParam.getString("branch"), requestParam.getString("duration"));
            } else {
                log.info(requestParam.getString("repoId") + ":已在扫描中");
            }
            return new ResponseBean(200, "start scan", null);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * @param requestParam 包含：repoId、branch、beginCommit
     */
    @PostMapping(value = {"/project/auto"})
    public ResponseBean autoScan(@RequestBody JSONObject requestParam) {
        try {
            String status = scanService.getScanStatus(requestParam.getString("repoId"), requestParam.getString("branch"));
            if (status == null || !status.equals("scanning")) {
                scanService.autoScan(requestParam.getString("repoId"), requestParam.getString("branch"), requestParam.getString("beginCommit"));
            } else {
                log.info(requestParam.getString("repoId") + ":已在扫描中");
            }
            return new ResponseBean(200, "start scan", null);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * 获取repo的扫描状态，参数：repoId,branch
     */
    @GetMapping(value = {"/project/scan/status"})
    public ResponseBean getScanStatus(@RequestParam("repoId") String repoId, @RequestParam("branch") String branch) {
        try {
            String data = scanService.getScanStatus(repoId, branch);
            return new ResponseBean(200, "" , data);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * @param requestParam 包含：repoId、branch
     */
    @PostMapping(value = {"/project/auto/update"})
    public ResponseBean autoUpdate(@RequestBody JSONObject requestParam) {
        try {
            String status = scanService.getScanStatus(requestParam.getString("repoId"), requestParam.getString("branch"));
            if (status != null && !status.equals("scanning")) {
                scanService.autoUpdate(requestParam.getString("repoId"), requestParam.getString("branch"));
            } else if (status == null){
                log.error("Update Scan Error: this repo hasn't been scanned!");
            } else {
                log.info(requestParam.getString("repoId") + ":已在扫描中");
            }
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

