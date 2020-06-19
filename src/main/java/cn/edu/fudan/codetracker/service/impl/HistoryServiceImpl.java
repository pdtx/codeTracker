package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.dao.HistoryDao;
import cn.edu.fudan.codetracker.domain.resultmap.MethodHistory;
import cn.edu.fudan.codetracker.domain.resultmap.MostModifiedInfo;
import cn.edu.fudan.codetracker.domain.resultmap.SurviveStatementInfo;
import cn.edu.fudan.codetracker.domain.resultmap.TempMostInfo;
import cn.edu.fudan.codetracker.service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HistoryServiceImpl implements HistoryService {
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
    public String getMethodUuid(MethodHistory methodHistory) {
        return historyDao.getMethodUuid(methodHistory);
    }

    @Override
    public MostModifiedInfo getMethodMetaInfo(String methodUuid){
        return historyDao.getMethodMetaInfo(methodUuid);
    }



}
