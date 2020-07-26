
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.constants.PublicConstants;
import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.service.StatisticsService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * description:
 * @author fancying
 * create: 2019-11-11 21:28
 **/
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService, PublicConstants {

    private StatisticsDao statisticsDao;


    @Override
    public Map<String,Integer> getValidLineCount(String repoUuid, String branch, String beginDate, String endDate) {
        Map<String,Integer> map = getValidLineMap(repoUuid, beginDate, endDate);
        return map;
    }

    private Map<String,Integer> getValidLineMap(String repoUuid, String beginDate, String endDate) {
        List<ValidLineInfo> list = new ArrayList<>();
        list.addAll(statisticsDao.getValidLineInfo(CLASS, repoUuid, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo(METHOD, repoUuid, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo(FIELD, repoUuid, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo(STATEMENT, repoUuid, beginDate, endDate));
        Map<String,Integer> map = new TreeMap<>();
        String lastMetaUuid = "";
        for (ValidLineInfo validInfo: list) {
            if (validInfo.getMetaUuid().equals(lastMetaUuid)) {
                continue;
            }
            if (validInfo.getChangeRelation().equals(DELETE)) {
                lastMetaUuid = validInfo.getMetaUuid();
                continue;
            }
            lastMetaUuid = validInfo.getMetaUuid();
            if (map.keySet().contains(validInfo.getCommitter())) {
                map.replace(validInfo.getCommitter(), map.get(validInfo.getCommitter())+1);
            } else {
                map.put(validInfo.getCommitter(), 1);
            }
        }
        return map;
    }



    @Override
    public Map<String,Map<String,Double>> getSurviveStatementStatistics(String beginDate, String endDate, String repoUuid, String branch) {
        Map<String,Map<String,Double>> map = new HashMap<>();
        Map<String,List<Long>> temp = statisticsDao.getSurviveStatementStatistics(beginDate, endDate, repoUuid, branch);
        for (String key : temp.keySet()) {
            List<Long> list = temp.get(key);
            list.sort((o1,o2) -> o1.compareTo(o2));
            Map<String,Double> newMap = new HashMap<>();
            newMap.put(MIN,(double)list.get(0));
            newMap.put(MAX,(double)list.get(list.size()-1));
            if (list.size()%2 == 0) {
                Double median = (list.get(list.size()/2) + list.get((list.size()/2)-1)) / 2.0;
                newMap.put(MEDIAN, new BigDecimal(median).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            } else {
                newMap.put(MEDIAN,(double)list.get((list.size()-1)/2));
            }
            Long sum = 0L;
            for (Long l : list) {
                sum += l;
            }
            Double average = sum / (list.size() * 1.0);
            newMap.put(AVERAGE,new BigDecimal(average).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            map.put(key,newMap);
        }
        return map;
    }


    @Override
    public void delete(String repoUuid, String branch) {
        statisticsDao.delete(repoUuid, branch);
    }


    @Override
    public Map<String,Map<String,Integer>> getAddDeleteStatementsNumber(String beginDate, String endDate, String repoUuid, String branch) {
        Map<String,Map<String,Integer>> result = new HashMap<>();
        List<ValidLineInfo> validLineInfos = new ArrayList<>();
        validLineInfos.addAll(statisticsDao.getValidLineInfo(CLASS,repoUuid,beginDate,endDate));
        validLineInfos.addAll(statisticsDao.getValidLineInfo(METHOD,repoUuid,beginDate,endDate));
        validLineInfos.addAll(statisticsDao.getValidLineInfo(FIELD,repoUuid,beginDate,endDate));
        validLineInfos.addAll(statisticsDao.getValidLineInfo(STATEMENT,repoUuid,beginDate,endDate));
        Map<String,ValidLineInfo> deleteMap = new HashMap<>();
        for (ValidLineInfo line: validLineInfos) {
            String changeRelation = line.getChangeRelation();
            Map<String,Integer> map;
            if(result.keySet().contains(line.getCommitter())) {
                map = result.get(line.getCommitter());
            } else {
                map = new HashMap<>();
                map.put(ADD,0);
                map.put(DELETE,0);
                map.put(CHANGE,0);
            }
            switch (changeRelation) {
                case ADD:
                    map.replace(ADD,map.get(ADD)+1);
                    break;
                case DELETE:
                    deleteMap.put(line.getMetaUuid(),line);
                    break;
                case SELF_CHANGE:
                    map.replace(CHANGE,map.get(CHANGE)+1);
                    break;
                default:
                    break;
            }
            result.put(line.getCommitter(),map);
        }
        for (ValidLineInfo validLineInfo : deleteMap.values()) {
            Map<String,Integer> map;
            if(result.keySet().contains(validLineInfo.getCommitter())) {
                map = result.get(validLineInfo.getCommitter());
            } else {
                map = new HashMap<>();
                map.put(ADD,0);
                map.put(DELETE,0);
                map.put(CHANGE,0);
            }
            map.put(DELETE, map.get(DELETE)+1);
            result.put(validLineInfo.getCommitter(),map);
        }
        return result;
    }

    @Override
    public List<JSONObject> getTop5LiveStatements(String repoUuid, String beginDate, String endDate) {
        Map<String,Integer> map = getValidLineMap(repoUuid, beginDate, endDate);
        SortedMap<Integer,List<String>> result = new TreeMap<Integer, List<String>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });
        for (String key : map.keySet()) {
            Integer value = map.get(key);
            if (result.keySet().contains(value)) {
                result.get(value).add(key);
            } else {
                List<String> list = new ArrayList<>();
                list.add(key);
                result.put(value,list);
            }
        }
        List<JSONObject> res = new ArrayList<>();
        int i = 0;
        for (Integer key: result.keySet()) {
            List<String> valueList = result.get(key);
            for (String name: valueList) {
                if (i >= 5) {
                    break;
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("counts",key);
                jsonObject.put("developer_name",name);
                res.add(jsonObject);
                i++;
            }
        }
        return res;
    }


    @Override
    public JSONObject getDeleteInfo(String beginDate,String endDate,String repoUuid) {
        JSONObject jsonObject = new JSONObject();
        Map<String,List<Long>> resultMap = statisticsDao.getDeleteInfo(beginDate, endDate, repoUuid);
        for (String developer: resultMap.keySet()) {
            List<Long> list = resultMap.get(developer);
            Long total = 0L;
            Long max = 0L;
            for (int i = 0; i < list.size() ; i++) {
                total += list.get(i);
                max = list.get(i) > max ? list.get(i) : max;
            }
            Double average = total/(list.size()*1.0);
            JSONObject person = new JSONObject();
            person.put(MAX,max);
            person.put(AVERAGE,new BigDecimal(average).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            jsonObject.put(developer,person);
        }
        return jsonObject;
    }


    @Override
    public JSONObject getFocusFileNum(String repoUuid, String beginDate, String endDate) {
        return getFileCount(repoUuid, beginDate, endDate);
    }

    private JSONObject getFileCount(String repoUuid, String beginDate, String endDate) {
        List<TempMostInfo> list = statisticsDao.getFocusFiles(repoUuid, beginDate, endDate);
        JSONObject jsonObject = new JSONObject();
        Map<String,Set<String>> map = new HashMap<>();
        Set<String> total = new HashSet<>();
        for (TempMostInfo info: list) {
            String committer = info.getCommitter();
            if (map.keySet().contains(committer)) {
                map.get(committer).add(info.getName());
            } else {
                Set<String> set = new HashSet<>();
                set.add(info.getName());
                map.put(committer,set);
            }
            total.add(info.getName());
        }
        JSONObject developer = new JSONObject();
        for (String key: map.keySet()) {
            developer.put(key,map.get(key).size());
        }
        jsonObject.put("total", total.size());
        jsonObject.put("developer", developer);
        return jsonObject;
    }

    @Override
    public JSONObject getFileNum(String repoUuid, String beginDate, String endDate) {
        JSONObject jsonObject = getFileCount(repoUuid, beginDate, endDate).getJSONObject("developer");
        return jsonObject;
    }


    @Override
    public JSONObject getChangeInfo(String beginDate,String endDate,String repoUuid) {
        Map<String,List<Long>> map = statisticsDao.getChangeInfo(repoUuid,beginDate,endDate);
        JSONObject jsonObject = new JSONObject();
        for (String key : map.keySet()) {
            List<Long> list = map.get(key);
            Long total = 0L;
            Long max = 0L;
            for (int i = 0; i < list.size() ; i++) {
                total += list.get(i);
                max = list.get(i) > max ? list.get(i) : max;
            }
            Double average = total/(list.size()*1.0);
            JSONObject person = new JSONObject();
            person.put(MAX,max);
            person.put(AVERAGE,new BigDecimal(average).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            jsonObject.put(key,person);
        }
        return jsonObject;
    }


    /**
     * getter and setter
     * */
    @Autowired
    public void setStatisticsDao(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }

}