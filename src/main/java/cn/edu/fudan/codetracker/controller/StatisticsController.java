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
import java.util.List;
import java.util.Map;

@RestController
@EnableAutoConfiguration
@Slf4j
public class StatisticsController {

    private StatisticsService statisticsService;
    private RestInterfaceManager restInterface;

    /**
     * 获取版本的统计信息
     * @param type package file class method
     */
    @GetMapping(value = {"/statistics/{type}/{repoId}"})
    public ResponseBean getStatistics(@PathVariable("type") String type, @PathVariable("repoId")String repoUuid, @RequestParam("branch") String branch) {
        try {
            List<VersionStatistics> data =  statisticsService.getStatistics(repoUuid, branch, type);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            log.error(e.getMessage());
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }
    /**
     * 获取修改最多的统计信息
     * @param type package file class method
     */
    @GetMapping(value = {"/statistics/modification/{type}/{repoId}"})
    public ResponseBean getMostModifiedStatistics(@PathVariable("type") String type, @PathVariable("repoId")String repoUuid, @RequestParam("branch") String branch) {
        try {
            List<MostModifiedInfo> data =  statisticsService.getMostModifiedInfo(repoUuid, branch, type);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            log.error(e.getMessage());
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }
    /**
     * 获取参与人员最多的统计信息
     * @param type package file class method
     */
    @GetMapping(value = {"/statistics/developer/{type}/{repoId}"})
    public ResponseBean getMostDevelopersInvolved(@PathVariable("type") String type, @PathVariable("repoId")String repoUuid, @RequestParam("branch") String branch) {
        try {
            List<MostDevelopersInfo> data =  statisticsService.getMostDevelopersInvolved(repoUuid, branch, type);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            log.error(e.getMessage());
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 根据时间限定获取修改最多的统计信息
     * @param type package file class method
     * @param beginDate xxxx-xx-xx
     * @param endDate xxxx-xx-xx
     */
    @GetMapping(value = {"/statistics/modificationtime/{type}/{repoId}"})
    public ResponseBean getMostModifiedByTime(@PathVariable("type") String type, @PathVariable("repoId") String repoUuid, @RequestParam("branch") String branch, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate) {
        try {
            List<MostDevelopersInfo> data = statisticsService.getMostModifiedByTime(repoUuid,branch,type,beginDate,endDate);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取某个package里面修改最多的method信息
     */
    @GetMapping(value = {"/statistics/modificationmethod/{repoId}/{packageId}"})
    public ResponseBean getMostModifiedMethod(@PathVariable("repoId") String repoUuid, @PathVariable("packageId") String packageUuid, @RequestParam("branch") String branch){
        try{
            List<MostModifiedMethod> data = statisticsService.getMostModifiedMethodByPackage(repoUuid, packageUuid, branch);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取指定时间内某个开发人员工作集中在何处
     */
    @GetMapping(value = {"/statistics/developerFocus/{type}"})
    public ResponseBean getDeveloperFocusMost(@PathVariable("type") String type, @RequestParam("committer") String committer, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate){
        try{
            List<DeveloperMostFocus> data = statisticsService.getDeveloperFocusMost(type, committer, beginDate, endDate);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取commit的时间线
     * @param type package file class method
     */
    @GetMapping(value = {"/statistics/timeline/{type}/{uuid}"})
    public ResponseBean getCommitTimeLine(@PathVariable("type") String type, @PathVariable("uuid") String uuid){
        try{
            List<CommitTimeLine> data = statisticsService.getCommitTimeLine(type, uuid);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取某个committer某段时间所删代码的相关信息
     */
    @GetMapping(value = {"/statistics/committer/delete/{committer}"})
    public ResponseBean getCommitHistoryByCommitter(@PathVariable("committer") String committer, @RequestParam("repoUuid") String repoUuid, @RequestParam("branch") String branch, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate){
        try{
            List<DeleteStatementInfo> data = statisticsService.getDeleteStatementFormerInfoByCommitter(committer, repoUuid, branch, beginDate, endDate);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取某个committer的commit历史
     */
    @GetMapping(value = {"/statistics/committer/{committer}"})
    public ResponseBean getDeleteStatementInfoByCommitter(@PathVariable("committer") String committer){
        try{
            List<CommitterHistory> data = statisticsService.getCommitHistoryByCommitter(committer);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 根据method、committer、时间段获取statement信息
     */
    @GetMapping(value = {"/statistics/committer/statement/{committer}/{methodUuid}"})
    public ResponseBean getDeleteStatementInfoByCommitter(@PathVariable("committer") String committer, @PathVariable("methodUuid") String methodUuid, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate){
        try{
            List<StatementInfoByMethod> data = statisticsService.getStatementInfoByMethod(committer, methodUuid, beginDate, endDate);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * 根据repoUuid, commit, repoPath, branch获取某个版本每个committer的贡献情况
     */
    @GetMapping(value = {"/statistics/committer/line/count"})
    public ResponseBean getChangeCommitterInfo(@RequestParam("repoUuid") String repoUuid, @RequestParam("commit") String commit, @RequestParam("repoPath") String repoPath, @RequestParam("branch") String branch){
        try{
            Map<String,Integer> data = statisticsService.getChangeCommitterInfo(repoUuid, commit, repoPath, branch);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * 根据repoUuid, commit, repoPath, branch, beginDate获取某个版本beginDate到commitDate期间每个committer的贡献情况
     */
    @GetMapping(value = {"/statistics/committer/line/count/period"})
    public ResponseBean getChangeCommitterInfoByDate(@RequestParam("repoUuid") String repoUuid, @RequestParam("commit") String commit, @RequestParam("repoPath") String repoPath, @RequestParam("branch") String branch, @RequestParam("beginDate") String beginDate){
        try{
            Map<String,Integer> data = statisticsService.getChangeCommitterInfoByDate(repoUuid, commit, repoPath, branch, beginDate);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 跟前端对接的接口，根据repoId,beginDate,endDate,committer(可选)获取期间贡献情况
     */
    @GetMapping(value = {"/statistics/committer/line/valid"})
    public ResponseBean getValidLineInfo(@RequestParam("repoUuid") String repoUuid, @RequestParam("branch") String branch, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate, @Param("developer") String developer){
        try{
            String commit = restInterface.getLatestCommit(repoUuid, beginDate, endDate);
            String repoPath = restInterface.getCodeServiceRepo(repoUuid, commit);
            log.info("commit: " + commit + "  repoPath: " + repoPath);
            Map<String,Integer> data = statisticsService.getChangeCommitterInfoByDate(repoUuid, commit, repoPath, branch, beginDate);
            restInterface.freeRepo(repoUuid, repoPath);
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
     * 根据repoUuid, commit, repoPath, branch获取截至该版本每个committer的代码增加、删除总情况
     */
    @GetMapping(value = {"/statistics/committer/line/total/count"})
    public ResponseBean getCommitterLineInfo(@RequestParam("repoUuid") String repoUuid, @RequestParam("commit") String commit, @RequestParam("repoPath") String repoPath, @RequestParam("branch") String branch){
        try{
            Map<String,Map<String,Integer>> data = statisticsService.getCommitterLineInfo(repoUuid, commit, repoPath, branch);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * 临时接口
     */
    @GetMapping(value = {"/statistics/committer/temp/focus"})
    public ResponseBean getFocus(@RequestParam("committer") String committer, @RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate, @RequestParam("repoUuid") String repoUuid, @RequestParam("branch") String branch){
        try{
            List<TempMostInfo> data = statisticsService.getFocus(committer, beginDate, endDate, repoUuid, branch);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * method历史接口
     */
    @GetMapping(value = {"/statistics/temp/method/history"})
    public ResponseBean getMethodHistory(@RequestParam("methodUuid") String methodUuid){
        try{
            List<MethodHistory> data = statisticsService.getMethodHistory(methodUuid);
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
    public ResponseBean getSurviveStatementStatistics(@RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate, @RequestParam("repoUuid") String repoUuid, @RequestParam("branch") String branch, @Param("developer") String developer){
        try{
            String begin = beginDate + " 00:00:00";
            String end = endDate + " 00:00:00";
            Map<String,Map<String,Long>> data = statisticsService.getSurviveStatementStatistics(begin, end, repoUuid, branch);
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
     * 获取语句历史
     * @param requestParam 包括methodUuid,以及语句body的数组
     * @return
     */
    @PostMapping(value = {"/statistics/statement/history"})
    public ResponseBean getStatementHistory(@RequestBody JSONObject requestParam) {
        try {
            String methodUuid = requestParam.getString("methodUuid");
            List<String> bodyList = new ArrayList<>();
            for (Object obj : requestParam.getJSONArray("statementList")) {
                bodyList.add(obj.toString());
            }
            Map<String,List<SurviveStatementInfo>> data = statisticsService.getStatementHistory(methodUuid, bodyList);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            e.printStackTrace();
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