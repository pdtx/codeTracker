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

import java.util.*;

@RestController
@EnableAutoConfiguration
@Slf4j
public class StatisticsController {

    private StatisticsService statisticsService;

    /**
     * 跟前端对接的接口，根据repoId,beginDate,endDate,committer(可选)获取期间贡献情况
     */
    @GetMapping(value = {"/statistics/committer/line/valid"})
    public ResponseBean getValidLineInfo(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer){
        try{
            List<String> dates= handleParamDate(beginDate, endDate);
            Map<String, Map<String, Integer>> data = statisticsService.getValidLineCount(repoUuid, branch, dates.get(0), dates.get(1), developer);
            if(developer != null && repoUuid == null){
                return new ResponseBean(200, "", data.get("total").get(developer));
            }else if(developer !=   null){
                return new ResponseBean(200, "", data.get(repoUuid).get(developer));
            }
            if(repoUuid != null){
                return new ResponseBean(200, "", data.get(repoUuid));
            }
            return new ResponseBean(200, "", data);
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
    public ResponseBean getSurviveStatementStatistics(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("developer") String developer){
        try{
            List<String> dates= handleParamDate(beginDate, endDate);
            Map<String,Map<String,Double>> data = statisticsService.getSurviveStatementStatistics(dates.get(0), dates.get(1), repoUuid, branch, developer);
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
     * 获取修改代码的年龄
     * @param beginDate
     * @param endDate
     * @param repoUuid
     * @param branch
     * @param developer
     * @return
     */
    @GetMapping(value = {"/statistics/changeInfo/lifecycle"})
    public ResponseBean getChangeStatementsLifecycle(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("developer") String developer){
        try{
            List<String> dates= handleParamDate(beginDate, endDate);
            Map<String, Map<String, Double>> data = statisticsService.getChangeStatementsLifecycle(dates.get(0), dates.get(1), repoUuid, branch, developer);
            if(developer != null){
                return new ResponseBean(200, "", data.get(developer));
            }
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取新增删除语句数
     * @param repoUuid
     * @param branch
     * @param beginDate
     * @param endDate
     * @return
     */
    @GetMapping(value = {"/statistics/statements"})
    public ResponseBean getAddAndDeleteStatements(@Param("repoUuid") String repoUuid, @Param("branch") String branch, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer) {
        try {
            List<String> dates= handleParamDate(beginDate, endDate);
            Map<String, Map<String,Map<String,Integer>>> data = statisticsService.getAddDeleteStatementsNumber(dates.get(0),dates.get(1),repoUuid,branch,developer);
            if(repoUuid != null){
                return new ResponseBean(200,"", data.get(repoUuid));
            }
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    @GetMapping(value = {"/statistics/delete/info"})
    public ResponseBean getDeleteInfo(@Param("repoUuid") String repoUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer) {
        try {
            List<String> dates= handleParamDate(beginDate, endDate);
            JSONObject data = statisticsService.getDeleteInfo(dates.get(0),dates.get(1),repoUuid,developer);
            if (developer == null) {
                return new ResponseBean(200,"",data);
            } else {
                return new ResponseBean(200,"",data.get(developer));
            }
        } catch (Exception e) {
            return new ResponseBean(401,e.getMessage(),null);
        }
    }

    @GetMapping(value = {"/statistics/top"})
    public ResponseBean getTop5Developer(@RequestParam("repoUuid") String repoUuid, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate) {
        try {
            String begin = beginDate + " 00:00:00";
            String end = endDate + " 24:00:00";
            List<JSONObject> data = statisticsService.getTop5LiveStatements(repoUuid, begin, end);
            return new ResponseBean(200,"",data);
        } catch (Exception e) {
            return new ResponseBean(401,e.getMessage(),null);
        }
    }


    @GetMapping(value = {"/statistics/focus/file/num"})
    public ResponseBean getFocusFileNum(@Param("repoUuid") String repoUuid, @Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("developer") String developer) {
        try {
            List<String> dates= handleParamDate(beginDate, endDate);
            JSONObject data = statisticsService.getFocusFileNum(repoUuid, dates.get(0), dates.get(1), developer);
            return new ResponseBean(200,"",data);
        } catch (Exception e) {
            return new ResponseBean(401,e.getMessage(),null);
        }
    }

    @GetMapping(value = {"/statistics/change/info"})
    public ResponseBean getChangeInfo(@RequestParam("repoUuid") String repoUuid, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate, @Param("developer") String developer) {
        try {
            String begin = beginDate + " 00:00:00";
            String end = endDate + " 24:00:00";
            JSONObject data = statisticsService.getChangeInfo(begin,end,repoUuid);
            if (developer == null) {
                return new ResponseBean(200,"",data);
            } else {
                return new ResponseBean(200,"",data.get(developer));
            }
        } catch (Exception e) {
            return new ResponseBean(401,e.getMessage(),null);
        }
    }

    @Autowired
    public void setStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 如果传入日期不存在，则获取全部日期的数据
     * @param beginDate
     * @param endDate
     * @return
     */
    private List<String> handleParamDate(String beginDate, String endDate){
        List<String> dates= new ArrayList<>(2);
        if(beginDate == null || endDate == null){
            beginDate= "1990-01-01";
            Calendar calendar = Calendar.getInstance();
            endDate= calendar.get(Calendar.YEAR)+ "-"+ (calendar.get(Calendar.MONTH)+ 1)+ "-" + calendar.get(Calendar.DATE);
        }
        dates.add(beginDate+ " 00:00:00");
        dates.add(endDate + " 24:00:00");
        return dates;
    }

}