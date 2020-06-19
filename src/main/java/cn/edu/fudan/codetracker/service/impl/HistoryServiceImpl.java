package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.dao.HistoryDao;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.service.HistoryService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HistoryServiceImpl implements HistoryService, PublicConstants {
    private HistoryDao historyDao;

    @Autowired
    public void setHistoryDao(HistoryDao historyDao) {
        this.historyDao = historyDao;
    }

    @Override
    public List<Map<String, Map<String, List<SurviveStatementInfo>>>> getStatementHistory(String methodUuid, List<String> statementBodyList) {
        List<Map<String,Map<String,List<SurviveStatementInfo>>>> mapList = new ArrayList<>();
        for (int i = 0; i < statementBodyList.size(); i++) {
            String commitId = statementBodyList.get(i).substring(0,40);
            String body = statementBodyList.get(i).substring(40);
            String queryBody = body + "%";
            List<SurviveStatementInfo> statementInfoList = historyDao.getStatementHistory(methodUuid, queryBody, commitId);
            Map<String,Map<String,List<SurviveStatementInfo>>> map = new HashMap<>();
            Map<String,List<SurviveStatementInfo>> m = new HashMap<>();
            m.put(commitId, statementInfoList);
            map.put(body, m);
            mapList.add(map);
        }
        return mapList;
    }


    @Override
    public List<Map<String,String>> getAllValidStatement(String methodUuid, String commitDate, String body) {
        return historyDao.getAllValidStatement(methodUuid, commitDate, body);
    }

    @Override
    public List<MethodHistory> getMethodHistory(String methodUuid) {
        return historyDao.getMethodHistory(methodUuid);
    }

    @Override
    public List<TempMostInfo> getFocus(String committer, String beginDate, String endDate, String repoUuid, String branch) {
        return historyDao.getFocus(committer,beginDate,endDate,repoUuid,branch);
    }


    @Override
    public MethodHistory getMethodInfo(String repoUuid, String filePath, String commitTime, String methodName) {
        return historyDao.getMethodInfo(repoUuid, filePath, commitTime, methodName);
    }



    @Override
    public MostModifiedInfo getMethodMetaInfo(String methodUuid){
        return historyDao.getMethodMetaInfo(methodUuid);
    }


    @Override
    public JSONObject getBugInfo(String repoUuid, String filePath, String commitTime, String methodName, String code) {
        MethodHistory methodHistory = historyDao.getMethodInfo(repoUuid, filePath, commitTime, methodName);
        JSONObject jsonObject = new JSONObject();
        //用repoUuid暂存methodUuid
        String methodUuid = methodHistory.getRepoUuid();
        jsonObject.put("methodUuid",methodUuid);
        jsonObject.put("commitId",methodHistory.getCommit());
        String[] strings = code.split("#");
        List<String> list = new ArrayList<>();
        for (String str: strings) {
            String s = "%" + str.trim() + "%";
            List<ValidLineInfo> statements = historyDao.getBugStatement(methodUuid,commitTime,s);
            String statement = null;
            ValidLineInfo lastLine = null;
            int min = Integer.MAX_VALUE;
            for (ValidLineInfo line: statements) {
                if (lastLine == null || !lastLine.getMetaUuid().equals(line.getMetaUuid())) {
                    if (!DELETE.equals(line.getChangeRelation())) {
                        int tmp = line.getEnd()-line.getBegin();
                        if (tmp < min) {
                            min = tmp;
                            statement = line.getBody();
                        }
                    }
                    lastLine = line;
                }
            }
            if (statement != null) {
                list.add(statement);
            }
        }
        jsonObject.put("statementList",list);
        return jsonObject;
    }

    @Override
    public List<TempMostInfo> getAllChangeInfo(String beginDate, String endDate, String repoUuid) {
        return historyDao.getAllChangeInfo(repoUuid, beginDate, endDate);
    }

    @Override
    public List<TempMostInfo> getChangeInfoByCommit(String repoUuid, String commitId) {
        return historyDao.getChangeInfoByCommit(repoUuid, commitId);
    }

    @Override
    public List<SurviveStatementInfo> getStatementHistoryByUuid(String methodUuid, String statementUuid) {
        return historyDao.getStatementHistoryByUuid(methodUuid, statementUuid);
    }


}
