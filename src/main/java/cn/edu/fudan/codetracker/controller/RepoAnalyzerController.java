package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.constants.ScanStatus;
import cn.edu.fudan.codetracker.domain.ResponseBean;
import cn.edu.fudan.codetracker.domain.projectinfo.ScanInfo;
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

    private ScanService scanService;


    /**
     * description 开始项目扫描 是否是第一次扫描应该由具体的服务决定 与调用的服务无关
     *
     * @param requestParam 包含： repoId、branch、commitId
     */
    @PostMapping(value = {"/codeTracker/codeTracker"})
    public ResponseBean scanByRequest(@RequestBody JSONObject requestParam) {
        String repoId = requestParam.getString("repoId");
        String branch = requestParam.getString("branch");
        String beginCommit = requestParam.getString("beginCommit");
        ScanInfo scanInfo = scanService.getScanInfo(repoId);
        try {
            if (beginCommit != null) {
                //首次扫描
                if (scanInfo != null) {
                    return new ResponseBean(HttpStatus.OK.value(), "error: already scanned before", null);
                }
                scanService.scan(repoId, branch, beginCommit);
                return new ResponseBean(HttpStatus.OK.value(), "start scan", null);
            } else {
                //更新
                if (scanInfo == null || scanInfo.getLatestCommit() == null) {
                    return new ResponseBean(HttpStatus.OK.value(), "error: not scanned before", null);
                }
                if (ScanStatus.SCANNING.equals(scanInfo.getStatus())) {
                    return new ResponseBean(HttpStatus.OK.value(), "already scanning", null);
                }
                scanService.autoUpdate(repoId, branch, scanInfo.getLatestCommit());
                return new ResponseBean(HttpStatus.OK.value(), "start scan", null);
            }
        } catch (Exception e) {
            return new ResponseBean(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }


    /**
     * description
     *
     * 获取repo的扫描状态，参数：repoId,branch
     */
    @GetMapping(value = {"/codeTracker/codeTracker/scan-status"})
    public ResponseBean getScanStatus(@RequestParam("repoId") String repoId) {
        try {
            ScanInfo data = scanService.getScanInfo(repoId);
            return new ResponseBean(HttpStatus.OK.value(), HttpStatus.OK.name(), data);
        } catch (Exception e) {
            return new ResponseBean(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
        }
    }




    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }


}

