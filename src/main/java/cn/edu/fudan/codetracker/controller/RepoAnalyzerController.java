package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.domain.ResponseBean;
import cn.edu.fudan.codetracker.service.ScanService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * description: 处理扫描请求
 *
 * @author fancying
 * create: 2019-09-19 10:40
 **/
@Slf4j
@RestController
@EnableAutoConfiguration
public class RepoAnalyzerController {

    private final String scanning = "scanning";

    private ScanService scanService;


    /**
     * description 开始项目扫描 是否是第一次扫描应该由具体的服务决定 与调用的服务无关
     *
     * @param requestParam 包含： repoId、branch、commitId
     */
    @PostMapping(value = {"/codeTracker"})
    public ResponseBean scan(@RequestBody JSONObject requestParam) {
        return ResponseBean.builder().build();
    }




    /**
     * description 开始项目扫描 是否是第一次扫描应该由具体的服务决定 与调用的服务无关
     *
     * @param requestParam 包含： repoId、branch、commitId
     */
    @PostMapping(value = {"/project/auto"})
    public ResponseBean scanByRequest(@RequestBody JSONObject requestParam) {
        String repoId = requestParam.getString("repoId");
        String branch = requestParam.getString("branch");
        String startCommit = requestParam.getString("beginCommit");
        try {
            String status = scanService.getScanStatus(repoId, branch);
            if (!scanning.equals(status)) {
                scanService.scan(repoId, branch, startCommit);
            } else {
                log.info("repo id[{}] :already scanning", repoId);
            }
            return new ResponseBean(HttpStatus.OK.value(), HttpStatus.OK.name(), null);
        } catch (Exception e) {
            return new ResponseBean(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }


    /**
     * description
     *
     * 获取repo的扫描状态，参数：repoId,branch
     */
    @GetMapping(value = {"/project/scan/status"})
    public ResponseBean getScanStatus(@RequestParam("repoId") String repoId, @RequestParam("branch") String branch) {
        try {
            String data = scanService.getScanStatus(repoId, branch);
            return new ResponseBean(HttpStatus.OK.value(), HttpStatus.OK.name(), data);
        } catch (Exception e) {
            return new ResponseBean(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }


    /**
     * description
     *
     * @param requestParam 包含：repoId、branch
     */
    @PostMapping(value = {"/project/auto/update"})
    public ResponseBean autoUpdate(@RequestBody JSONObject requestParam) {
        String repoId = requestParam.getString("repoId");
        String branch = requestParam.getString("branch");
        try {
            String status = scanService.getScanStatus(repoId, branch);
            if (status != null && !scanning.equals(status)) {
                scanService.autoUpdate(repoId, branch);
            } else if (status == null){
                log.error("Update Scan Error: this repo hasn't been scanned!");
            } else {
                log.info("repo id[{}] :already scanning", repoId);
            }
            return new ResponseBean(HttpStatus.OK.value(), HttpStatus.OK.name(), null);
        } catch (Exception e) {
            return new ResponseBean(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }



    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }


}

