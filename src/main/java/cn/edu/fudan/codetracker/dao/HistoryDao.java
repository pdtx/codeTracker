package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.mapper.HistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HistoryDao {
    private HistoryMapper historyMapper;

    @Autowired
    public void setHistoryMapper(HistoryMapper historyMapper) {
        this.historyMapper = historyMapper;
    }

    /**
     * 获取语句历史切片
     */
    public List<SurviveStatementInfo> getStatementHistory(String methodUuid, String body, String commitId) {
        List<SurviveStatementInfo> statementInfoList = historyMapper.getStatementHistory(methodUuid, body, commitId);
        List<SurviveStatementInfo> addList = new ArrayList<>();
        List<MethodHistory> methodHistoryList = historyMapper.getMethodHistory(methodUuid);
        Map<String,SurviveStatementInfo> map = new HashMap<>();
        Map<String,MethodHistory> mapMethod = new HashMap<>();
        for (SurviveStatementInfo surviveStatementInfo : statementInfoList) {
            if (surviveStatementInfo.getChangeRelation().equals("DELETE")) {
                surviveStatementInfo.setBody(null);
                surviveStatementInfo.setBegin(-1);
                surviveStatementInfo.setEnd(-1);
            }
            map.put(surviveStatementInfo.getCommit(),surviveStatementInfo);
        }
        for (MethodHistory methodHistory : methodHistoryList) {
            mapMethod.put(methodHistory.getCommit(),methodHistory);
        }

        SurviveStatementInfo lastSurviveStatement = null;
        for (MethodHistory methodHistory : methodHistoryList) {
            if (!map.keySet().contains(methodHistory.getCommit())) {
                if (lastSurviveStatement != null) {
                    if (mapMethod.keySet().contains(lastSurviveStatement.getCommit())) {
                        MethodHistory lastMethodHistory = mapMethod.get(lastSurviveStatement.getCommit());
                        int begin = -1;
                        int end = -1;
                        int cha = -1;
                        int methodLines = 0;
                        int beginInMethod = lastSurviveStatement.getBegin() - lastMethodHistory.getMethodBegin();
                        String bodyStr = lastSurviveStatement.getBody();
                        int lines = bodyStr.split("\\n").length - 1;
                        String contentStr = methodHistory.getContent();
                        while (contentStr.indexOf(bodyStr) != -1) {
                            int loc = contentStr.indexOf(bodyStr);
                            String tmp = contentStr.substring(0,loc);
                            if (cha == -1) {
                                methodLines = tmp.split("\\n").length;
                                begin = methodLines;
                                end = begin + lines;
                                cha = begin-beginInMethod > 0 ? begin-beginInMethod : beginInMethod-begin;
                            } else {
                                methodLines = methodLines + lines + tmp.split("\\n").length;
                                int bTmp = methodLines + 1;
                                int cTmp = bTmp-beginInMethod > 0 ? bTmp-beginInMethod : beginInMethod-bTmp;
                                if (cTmp < cha) {
                                    begin = bTmp;
                                    end = begin + lines;
                                    cha = cTmp;
                                }
                            }
                            contentStr = contentStr.substring(loc+bodyStr.length());
                        }
                        if (begin != -1 && end != -1) {
                            SurviveStatementInfo statementInfo = new SurviveStatementInfo();
                            statementInfo.setBegin(begin);
                            statementInfo.setEnd(end);
                            statementInfo.setCommit(methodHistory.getCommit());
                            addList.add(statementInfo);
                        }
                    }
                }
            } else {
                lastSurviveStatement = map.get(methodHistory.getCommit());
            }
        }

        if (addList.size() != 0) {
            statementInfoList.addAll(addList);
        }

        return statementInfoList;
    }


    /**
     * 一次性获取全部可选语句
     */
    public List<Map<String,String>> getAllValidStatement(String methodUuid, String commitDate, String body) {
        String[] strs = body.split("\\n");
        List<Map<String,String>> mapList = new ArrayList<>();
        List<String> list = new ArrayList<>();

        List<StatementInfoByMethod> statementInfoByMethodList = historyMapper.getAllValidStatement(methodUuid, commitDate);
        String lastStatementUuid = "";
        for (StatementInfoByMethod statementInfoByMethod : statementInfoByMethodList) {
            if (!statementInfoByMethod.getStatementUuid().equals(lastStatementUuid)) {
                if(!"DELETE".equals(statementInfoByMethod.getChangeRelation())) {
                    list.add(statementInfoByMethod.getBody());
                }
                lastStatementUuid = statementInfoByMethod.getStatementUuid();
            }
        }

        for (String str : strs) {
            boolean find = false;
            String tmp = str.trim();
            Map<String,String> map = new HashMap<>();
            if ("".equals(tmp)) {
                map.put(str, null);
            } else {
                for (String s : list) {
                    if (s.startsWith(tmp) || tmp.startsWith(s)) {
                        map.put(str, s);
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    map.put(str, null);
                }
            }
            mapList.add(map);
        }
        return mapList;
    }


    /**
     * 临时接口
     */
    public List<MethodHistory> getMethodHistory(String methodUuid) {
        return historyMapper.getMethodHistory(methodUuid);
    }

    /**
     * 临时接口
     */
    public List<TempMostInfo> getFocus(String committer, String beginDate, String endDate, String repoUuid, String branch) {
        List<MostModifiedInfo> packageInfos = historyMapper.getPackageInfoMost(committer, beginDate, endDate, repoUuid, branch);
        List<TempMostInfo> packageList = new ArrayList<>();
        for (MostModifiedInfo mostModifiedInfo: packageInfos) {
            TempMostInfo packageInfo = new TempMostInfo();
            packageInfo.setName(mostModifiedInfo.getPackageName());
            packageInfo.setQuantity(mostModifiedInfo.getVersion());
            packageInfo.setUuid(mostModifiedInfo.getUuid());
            List<MostModifiedInfo> classInfos = historyMapper.getClassInfoMost(committer,mostModifiedInfo.getModuleName(),mostModifiedInfo.getPackageName(),beginDate,endDate,repoUuid,branch);
            List<TempMostInfo> classList = new ArrayList<>();
            for (MostModifiedInfo modifiedInfo : classInfos) {
                TempMostInfo classInfo = new TempMostInfo();
                classInfo.setName(modifiedInfo.getClassName());
                classInfo.setQuantity(modifiedInfo.getVersion());
                classInfo.setUuid(modifiedInfo.getUuid());
                List<MostModifiedInfo> methodInfos = historyMapper.getMethodInfoMost(committer,modifiedInfo.getFilePath(),modifiedInfo.getClassName(),beginDate,endDate,repoUuid,branch);
                List<TempMostInfo> methodList = new ArrayList<>();
                for (MostModifiedInfo methodInfo : methodInfos) {
                    TempMostInfo method = new TempMostInfo();
                    method.setName(methodInfo.getMethodName());
                    method.setQuantity(methodInfo.getVersion());
                    method.setChildInfos(null);
                    method.setUuid(methodInfo.getUuid());
                    methodList.add(method);
                }
                classInfo.setChildInfos(methodList);
                classList.add(classInfo);
            }
            packageInfo.setChildInfos(classList);
            packageList.add(packageInfo);
        }
        return packageList;
    }
}
