/**
 * @description:
 * @author: fancying
 * @create: 2019-11-11 21:25
 **/
package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.component.RestInterfaceManager;
import cn.edu.fudan.codetracker.domain.ResponseBean;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.service.StatisticsService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@EnableAutoConfiguration
@Slf4j
public class StatisticsController {

    private StatisticsService statisticsService;
    private RestInterfaceManager restInterface;

    /**
     * 跟前端对接的接口，根据repoId,beginDate,endDate,committer(可选)获取期间贡献情况
     */
    @GetMapping(value = {"/statistics/committer/line/valid"})
    public ResponseBean getValidLineInfo(@RequestParam("repoUuid") String repoUuid, @RequestParam("branch") String branch, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate, @Param("developer") String developer){
        try{
            String begin = beginDate + " 00:00:00";
            String end = endDate + " 24:00:00";
            Map<String,Integer> data = statisticsService.getValidLineCount(repoUuid, branch, begin, end);
            if (developer == null) {
                return new ResponseBean(200, "", data);
            } else {
                return new ResponseBean(200, "", data.get(developer));
            }
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 统计存活周期
     */
    @GetMapping(value = {"/statistics/lifecycle"})
    public ResponseBean getSurviveStatementStatistics(@RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate, @RequestParam("repoUuid") String repoUuid, @RequestParam("branch") String branch, @Param("developer") String developer){
        try{
            String begin = beginDate + " 00:00:00";
            String end = endDate + " 24:00:00";
            Map<String,Map<String,Double>> data = statisticsService.getSurviveStatementStatistics(begin, end, repoUuid, branch);
            if (developer == null) {
                return new ResponseBean(200, "", data);
            } else {
                return new ResponseBean(200, "" , data.get(developer));
            }

        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 删除操作
     */
    @DeleteMapping(value = {"/codetracker"})
    public ResponseBean delete(@RequestParam("repoUuid") String repoUuid, @RequestParam("branch") String branch) {
        try{
            statisticsService.delete(repoUuid, branch);
            return new ResponseBean(200, "delete success", null);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    @Autowired
    public void setStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface) {
        this.restInterface = restInterface;
    }
}