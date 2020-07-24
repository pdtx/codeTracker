package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.domain.ResponseBean;
import cn.edu.fudan.codetracker.domain.resultmap.MethodHistory;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.SurviveStatementInfo;
import cn.edu.fudan.codetracker.domain.resultmap.TempMostInfo;
import cn.edu.fudan.codetracker.service.HistoryService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@EnableAutoConfiguration
@Slf4j
public class HistoryController {
    private HistoryService historyService;

    @Autowired
    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
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
            List<String> statementList = new ArrayList<>();
            for (Object object : requestParam.getJSONArray("statementList")) {
                statementList.add((String)object);
            }
            List<Map<String, Map<String,List<SurviveStatementInfo>>>> data = historyService.getStatementHistory(methodUuid, statementList);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * 获取全部可选语句
     */
    @PostMapping(value = {"/valid/statement"})
    public ResponseBean getAllValidStatement(@RequestBody JSONObject requestParam) {
        try {
            List<Map<String,String>> data = historyService.getAllValidStatement(requestParam.getString("methodUuid"), requestParam.getString("commitDate"), requestParam.getString("body"));
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * method历史接口
     */
    @GetMapping(value = {"/statistics/temp/method/history"})
    public ResponseBean getMethodHistory(@RequestParam("methodUuid") String methodUuid){
        try{
            List<MethodHistory> data = historyService.getMethodHistory(methodUuid);
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
            String begin = beginDate + " 00:00:00";
            String end = endDate + " 24:00:00";
            List<TempMostInfo> data = historyService.getFocus(committer, begin, end, repoUuid, branch);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 根据issueList中信息查找对应的methodUuid，有filePath,commitId,issue行号,repoUuid等
     */
    @PostMapping(value = {"/history/issue/method"})
    public ResponseBean getMethodUuid(@RequestBody JSONObject body) {
        try {
            JSONObject data = historyService.getBugInfo(body.getString("repoUuid"), body.getString("filePath"), body.getString("commitTime"), body.getString("methodName"), body.getString("code"));
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取method的package、class等信息
     */
    @GetMapping(value = {"/history/issue/method/meta"})
    public ResponseBean getMethodMetaInfo(@RequestParam("methodUuid") String methodUuid){
        try {
            MostModifiedInfo data = historyService.getMethodMetaInfo(methodUuid);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * 获取全部修改的file method信息
     */
    @GetMapping(value = {"/history/all"})
    public ResponseBean getAllChangeInfo(@RequestParam("beginDate") String beginDate, @RequestParam("endDate") String endDate, @RequestParam("repoUuid") String repoUuid){
        try{
            String begin = beginDate + " 00:00:00";
            String end = endDate + " 24:00:00";
            List<TempMostInfo> data = historyService.getAllChangeInfo(begin,end,repoUuid);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

    /**
     * 获取某次修改信息
     */
    @GetMapping(value = {"/history/commit"})
    public ResponseBean getChangeInfoByCommit(@RequestParam("commitId") String commitId, @RequestParam("repoUuid") String repoUuid){
        try{
            List<TempMostInfo> data = historyService.getChangeInfoByCommit(repoUuid, commitId);
            return new ResponseBean(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean(401, e.getMessage(), null);
        }
    }



    /**
     * 获取语句历史 by uuid
     */
    @GetMapping(value = {"/statistics/statement/history/uuid"})
    public ResponseBean getStatementHistoryByUuid(@RequestParam("methodUuid") String methodUuid, @RequestParam("statementUuid") String statementUuid) {
        try {
            List<SurviveStatementInfo> data = historyService.getStatementHistoryByUuid(methodUuid, statementUuid);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, e.getMessage(), null);
        }
    }


    /**
     * 获取方法基于圈复杂度切片历史
     */
    @GetMapping(value = {"/history/method/ccn"})
    public ResponseBean getMethodHistoryByCcn(@RequestParam("methodUuid") String methodUuid) {
        try {
            List<MethodHistory> data = historyService.getMethodHistoryByCcn(methodUuid);
            return new ResponseBean(200, "", data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseBean(401, e.getMessage(), null);
        }
    }

}
