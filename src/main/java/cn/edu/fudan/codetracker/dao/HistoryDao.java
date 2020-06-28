package cn.edu.fudan.codetracker.dao;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.mapper.HistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

@Repository
public class HistoryDao implements PublicConstants {
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
        List<MethodHistory> methodHistoryList = historyMapper.getMethodHistory(methodUuid);
        return getStatementInfos(statementInfoList, methodHistoryList);
    }

    /**
     * 获取语句历史切片
     */
    public List<SurviveStatementInfo> getStatementHistoryByUuid(String methodUuid, String statementUuid) {
        List<SurviveStatementInfo> statementInfoList = historyMapper.getStatementHistoryById(statementUuid);
        List<MethodHistory> methodHistoryList = historyMapper.getMethodHistory(methodUuid);
        return getStatementInfos(statementInfoList, methodHistoryList);
    }

    private List<SurviveStatementInfo> getStatementInfos(List<SurviveStatementInfo> statementInfoList, List<MethodHistory> methodHistoryList) {
        List<SurviveStatementInfo> addList = new ArrayList<>();
        Map<String,SurviveStatementInfo> map = new HashMap<>();
        Map<String,MethodHistory> mapMethod = new HashMap<>();
        for (SurviveStatementInfo surviveStatementInfo : statementInfoList) {
            if (surviveStatementInfo.getChangeRelation().equals(DELETE)) {
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
                        if (bodyStr == null) {
                            continue;
                        }
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
                if(!DELETE.equals(statementInfoByMethod.getChangeRelation())) {
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
        List<MethodHistory> methodHistoryList = historyMapper.getMethodHistory(methodUuid);
        for (MethodHistory method: methodHistoryList) {
            if (method.getChangeRelation().equals(DELETE)) {
                method.setContent(null);
                method.setDiff(null);
                method.setMethodBegin(-1);
                method.setMethodEnd(-1);
            }
        }
        return methodHistoryList;
    }

    /**
     * 临时接口
     */
    public List<TempMostInfo> getFocus(String committer, String beginDate, String endDate, String repoUuid, String branch) {
        List<MostModifiedInfo> packageInfos = historyMapper.getPackageInfoMost(committer, beginDate, endDate, repoUuid, branch);
        List<TempMostInfo> packageList = new ArrayList<>();
        //包
        for (MostModifiedInfo mostModifiedInfo: packageInfos) {
            TempMostInfo packageInfo = new TempMostInfo();
            packageInfo.setName(mostModifiedInfo.getPackageName());
            packageInfo.setQuantity(mostModifiedInfo.getVersion());
            packageInfo.setUuid(mostModifiedInfo.getUuid());
            List<TempMostInfo> fileList = new ArrayList<>();
            List<MostModifiedInfo> fileInfos = historyMapper.getFileInfoMost(committer,mostModifiedInfo.getPackageName(),beginDate,endDate,repoUuid,branch);
            //文件
            for (MostModifiedInfo fileMostInfo : fileInfos) {
                TempMostInfo fileInfo = new TempMostInfo();
                fileInfo.setName(fileMostInfo.getFileName());
                fileInfo.setQuantity(fileMostInfo.getVersion());
                fileInfo.setUuid(fileMostInfo.getUuid());
                List<MostModifiedInfo> classInfos = historyMapper.getClassInfoMost(committer,fileMostInfo.getFileName(),beginDate,endDate,repoUuid,branch);
                List<TempMostInfo> classList = new ArrayList<>();
                //类
                for (MostModifiedInfo modifiedInfo : classInfos) {
                    TempMostInfo classInfo = new TempMostInfo();
                    classInfo.setName(modifiedInfo.getClassName());
                    classInfo.setQuantity(modifiedInfo.getVersion());
                    classInfo.setUuid(modifiedInfo.getUuid());
                    List<MostModifiedInfo> methodInfos = historyMapper.getMethodInfoMost(committer,modifiedInfo.getFilePath(),modifiedInfo.getClassName(),beginDate,endDate,repoUuid,branch);
                    List<TempMostInfo> methodList = new ArrayList<>();
                    //方法
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
                fileInfo.setChildInfos(classList);
                fileList.add(fileInfo);
            }
            packageInfo.setChildInfos(fileList);
            packageList.add(packageInfo);
        }
        return packageList;
    }

    /**
     * 根据issueList中信息查找对应的methodUuid，有filePath,commitId,issue行号,repoUuid等
     */
    public MethodHistory getMethodInfo(String repoUuid, String filePath, String commitTime, String methodName) {
        methodName = methodName + '%';
        return historyMapper.getMethodInfo(repoUuid, filePath, commitTime, methodName);
    }


    /**
     * 获取method的package、class等信息
     */
    public MostModifiedInfo getMethodMetaInfo(String methodUuid){
        return historyMapper.getMethodMetaInfo(methodUuid);
    }

    /**
     * 获取bug所在statement
     */
    public List<ValidLineInfo> getBugStatement(String methodUuid, String commitTime, String body) {
        return historyMapper.getBugStatement(methodUuid, commitTime, body);
    }

    public List<TempMostInfo> getAllChangeInfo(String repoUuid, String beginDate, String endDate) {
        List<TempMostInfo> result = new ArrayList<>();
        Map<String, List<MostModifiedInfo>> methodMap = new HashMap<>();
        List<MostModifiedInfo> fileInfos = historyMapper.getFileInfo(beginDate, endDate, repoUuid);
        List<MostModifiedInfo> methodInfos = historyMapper.getMethodInfoByFile(beginDate,endDate,repoUuid);
        for (MostModifiedInfo method : methodInfos) {
            if (methodMap.keySet().contains(method.getFilePath())) {
                methodMap.get(method.getFilePath()).add(method);
            } else {
                List<MostModifiedInfo> list = new ArrayList<>();
                list.add(method);
                methodMap.put(method.getFilePath(), list);
            }
        }
        for (MostModifiedInfo file : fileInfos) {
            TempMostInfo fileInfo = new TempMostInfo();
            fileInfo.setUuid(file.getUuid());
            fileInfo.setName(file.getFileName());
            fileInfo.setFilePath(file.getFilePath());
            List<TempMostInfo> methods = new ArrayList<>();
            if (methodMap.get(file.getFilePath()) != null) {
                for (MostModifiedInfo method : methodMap.get(file.getFilePath())) {
                    TempMostInfo methodInfo = new TempMostInfo();
                    methodInfo.setUuid(method.getUuid());
                    methodInfo.setName(method.getMethodName());
                    methods.add(methodInfo);
                }
            }
            fileInfo.setChildInfos(methods);
            result.add(fileInfo);
        }
        return result;
    }

    public List<TempMostInfo> getChangeInfoByCommit(String repoUuid, String commitId) {
        List<TempMostInfo> result = new ArrayList<>();
        Map<String, List<MostModifiedInfo>> methodMap = new HashMap<>();
        Map<String, List<MostModifiedInfo>> statementMap = new HashMap<>();
        Map<String, MostModifiedInfo> lastMethodMap = new HashMap<>();
        List<MostModifiedInfo> fileInfos = historyMapper.getFileInfoByCommit(repoUuid, commitId);
        List<MostModifiedInfo> methodInfos = historyMapper.getMethodInfoByCommit(repoUuid, commitId);
        List<MostModifiedInfo> statementInfos = historyMapper.getStatementInfoByCommit(repoUuid, commitId);
        Set<String> methodUuidList = new HashSet<>();
        for (MostModifiedInfo method : methodInfos) {
            if (methodMap.keySet().contains(method.getFilePath())) {
                methodMap.get(method.getFilePath()).add(method);
            } else {
                List<MostModifiedInfo> list = new ArrayList<>();
                list.add(method);
                methodMap.put(method.getFilePath(), list);
            }
            methodUuidList.add(method.getUuid());
        }
        List<MostModifiedInfo> lastMethodInfos = historyMapper.getMethodLastInfo(methodUuidList, commitId);
        for (MostModifiedInfo lastMethod : lastMethodInfos) {
            if (!lastMethodMap.keySet().contains(lastMethod.getUuid())) {
                lastMethodMap.put(lastMethod.getUuid(), lastMethod);
            }
        }
        for (MostModifiedInfo statement : statementInfos) {
            if (statementMap.keySet().contains(statement.getMethodUuid())) {
                statementMap.get(statement.getMethodUuid()).add(statement);
            } else {
                List<MostModifiedInfo> list = new ArrayList<>();
                list.add(statement);
                statementMap.put(statement.getMethodUuid(), list);
            }
        }
        for (MostModifiedInfo file : fileInfos) {
            TempMostInfo fileInfo = new TempMostInfo();
            fileInfo.setUuid(file.getUuid());
            fileInfo.setName(file.getFileName());
            fileInfo.setChangeRelation(file.getChangeRelation());
            fileInfo.setFilePath(file.getFilePath());
            List<TempMostInfo> methods = new ArrayList<>();
            if (methodMap.get(file.getFilePath()) != null) {
                for (MostModifiedInfo method : methodMap.get(file.getFilePath())) {
                    TempMostInfo methodInfo = new TempMostInfo();
                    methodInfo.setUuid(method.getUuid());
                    methodInfo.setName(method.getMethodName());
                    methodInfo.setChangeRelation(method.getChangeRelation());
                    int methodBegin = method.getBegin();
                    int methodHeight = method.getEnd() - methodBegin + 1;
                    //method真实行号、长度信息
                    methodInfo.setLineBegin(method.getBegin());
                    methodInfo.setLineEnd(method.getEnd());
                    methodInfo.setLineHeight(methodHeight);
                    int lastBegin = 0;
                    int lastHeight = 0;
                    List<TempMostInfo> statements = new ArrayList<>();
                    if (statementMap.get(method.getUuid()) != null) {
                        MostModifiedInfo lastStat = null;
                        for (MostModifiedInfo statement : statementMap.get(method.getUuid())) {
                            if (lastStat != null && statement.getBegin() >= lastStat.getBegin() && statement.getEnd() <= lastStat.getEnd()) {
                                continue;
                            }
                            TempMostInfo statementInfo = new TempMostInfo();
                            statementInfo.setUuid(statement.getUuid());
                            statementInfo.setChangeRelation(statement.getChangeRelation());
                            statementInfo.setDescription(statement.getDescription());
                            //statement真实行号、长度信息
                            statementInfo.setLineBegin(statement.getBegin());
                            statementInfo.setLineEnd(statement.getEnd());
                            statementInfo.setLineHeight(statement.getEnd()-statement.getBegin()+1);
                            //statement相对method的起始位置、高度占比
                            double begin,height;
                            if (DELETE.equals(statement.getChangeRelation())) {
                                if (lastBegin == 0 && lastHeight == 0) {
                                    MostModifiedInfo lastMethod = lastMethodMap.get(method.getUuid());
                                    if (lastMethod != null) {
                                        lastBegin = lastMethod.getBegin();
                                        lastHeight = lastMethod.getEnd() - lastBegin + 1;
                                    }
                                }
                                begin = (statement.getBegin()-lastBegin)*1.0/lastHeight;
                                height = (statement.getEnd()-statement.getBegin()+1)*1.0/lastHeight;
                            } else {
                                begin = (statement.getBegin()-methodBegin)*1.0/methodHeight;
                                height = (statement.getEnd()-statement.getBegin()+1)*1.0/methodHeight;
                            }
                            statementInfo.setBegin(new BigDecimal(begin).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue());
                            statementInfo.setHeight(new BigDecimal(height).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue());
                            statements.add(statementInfo);
                            lastStat = statement;
                        }
                    }
                    methodInfo.setChildInfos(statements);
                    methods.add(methodInfo);
                }
            }
            fileInfo.setChildInfos(methods);
            result.add(fileInfo);
        }
        return result;
    }


}
