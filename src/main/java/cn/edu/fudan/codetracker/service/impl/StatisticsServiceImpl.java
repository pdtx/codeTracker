
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
    public Map<String, Map<String,Integer>> getValidLineCount(String repoUuid, String branch, String beginDate, String endDate, String developer) {
        Map<String, Map<String,Integer>> map = getValidLineMap(repoUuid, beginDate, endDate, developer);
        Map<String, Integer> developerMap= new TreeMap<>();
        for(String repo : map.keySet()){
            Integer sum= 0;
            for(String committer : map.get(repo).keySet()){
                sum+= map.get(repo).get(committer);
                if(developerMap.containsKey(committer)){
                    developerMap.replace(committer, developerMap.get(committer)+ map.get(repo).get(committer));
                }else{
                    developerMap.put(committer, map.get(repo).get(committer));
                }
            }
            map.get(repo).put("total", sum);
        }
        map.put("total", developerMap);
        return map;
    }

    private Map<String, Map<String,Integer>> getValidLineMap(String repoUuid, String beginDate, String endDate, String developer) {
        List<ValidLineInfo> list = statisticsDao.getValidLineInfo(repoUuid, beginDate, endDate, developer);
        Map<String, Map<String,Integer>> map = new TreeMap<>();
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
            if(map.keySet().contains(validInfo.getRepoUuid())){
                if(map.get(validInfo.getRepoUuid()).keySet().contains(validInfo.getCommitter())){
                    map.get(validInfo.getRepoUuid()).replace(validInfo.getCommitter(), map.get(validInfo.getRepoUuid()).get(validInfo.getCommitter())+ 1);
                }else {
                    map.get(validInfo.getRepoUuid()).put(validInfo.getCommitter(), 1);
                }
            }else {
                Map<String, Integer> developerMap= new TreeMap<>();
                developerMap.put(validInfo.getCommitter(), 1);
                map.put(validInfo.getRepoUuid(), developerMap);
            }
        }
        return map;
    }

    @Override
    public Map<String, Map<String, Double>> getChangeStatementsLifecycle(String beginDate, String endDate, String repoUuid, String branch){
        return getMeasureInfoFromList(statisticsDao.getChangeStatementsInfo(beginDate, endDate, repoUuid, branch));
    }

    @Override
    public Map<String,Map<String,Double>> getSurviveStatementStatistics(String beginDate, String endDate, String repoUuid, String branch) {
        return getMeasureInfoFromList(statisticsDao.getSurviveStatementStatistics(beginDate, endDate, repoUuid, branch));
    }

    /**
     * 获取年龄数据中的最大值，最小值，平均数，中位数，上四分位和下四分位
     * @param map
     * @return key: committer, value: {MAX, MIN, AVERAGE, MEDIAN, UPPER_QUARTILE, LOWER_QUARTILE}
     */
    private Map<String, Map<String, Double>> getMeasureInfoFromList(Map<String, List<Long>> map){
        Map<String,Map<String,Double>> measureResult = new HashMap<>(16);
        for(String committer: map.keySet()){
            List<Long> list= map.get(committer);
            list.sort((o1, o2) -> o1.compareTo(o2));
            Map<String, Double> committerMap= new HashMap<>();
            committerMap.put(MIN,(double)list.get(0));
            committerMap.put(MAX,(double)list.get(list.size()-1));
            Long sum = 0L;
            for (Long l : list) {
                sum += l;
            }
            double average = sum / (list.size() * 1.0);
            committerMap.put(AVERAGE,new BigDecimal(average).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            if (list.size()%2 == 0) {
                double median = (list.get(list.size()/2) + list.get((list.size()/2)-1)) / 2.0;
                committerMap.put(MEDIAN, new BigDecimal(median).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                setQuartiles(list, committerMap);
            } else {
                // 若总个数为奇数且大于1，则移除中位数后，再计算上下四分位
                committerMap.put(MEDIAN,(double)list.get((list.size()-1)/2));
                if(list.size()> 1){
                    list.remove((list.size() -1)/2);
                    setQuartiles(list, committerMap);
                }else{
                    committerMap.put(LOWER_QUARTILE, (double)list.get((list.size()-1)/2));
                    committerMap.put(UPPER_QUARTILE, (double)list.get((list.size()-1)/2));
                }
            }
            measureResult.put(committer, committerMap);
        }
        return measureResult;
    }

    private void setQuartiles(List<Long> list, Map<String, Double> map){
        int divide= 2;
        if((list.size()/divide) % divide == 0){
            double lowerQuartile= (list.get(list.size()/4)+ list.get(list.size()/4 -1)) / 2.0;
            double upperQuartile= (list.get(list.size()/4* 3)+ list.get(list.size()/4* 3 -1)) / 2.0;
            map.put(LOWER_QUARTILE, new BigDecimal(lowerQuartile).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            map.put(UPPER_QUARTILE, new BigDecimal(upperQuartile).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        }else {
            map.put(LOWER_QUARTILE, (double)list.get((list.size()/2- 1)/2));
            map.put(UPPER_QUARTILE, (double)list.get(list.size()/2 + (list.size()/2- 1)/2));
        }
    }

    @Override
    public void delete(String repoUuid, String branch) {
        statisticsDao.delete(repoUuid, branch);
    }


    @Override
    public Map<String, Map<String,Map<String,Integer>>> getAddDeleteStatementsNumber(String beginDate, String endDate, String repoUuid, String branch, String developer) {
        Map<String, Map<String,Map<String,Integer>>> result = new HashMap<>(16);
        List<ValidLineInfo> validLineInfos =statisticsDao.getValidLineInfo(repoUuid,beginDate,endDate, developer);
        Map<String, Map<String, String>> firstCommitterMap= statisticsDao.getFirstCommitter();
        Map<String,ValidLineInfo> deleteMap = new HashMap<>();
        for (ValidLineInfo line: validLineInfos) {
            String changeRelation = line.getChangeRelation();
            Map<String,Integer> map;
            if(result.keySet().contains(line.getRepoUuid()) && result.get(line.getRepoUuid()).containsKey(line.getCommitter())){
                    map= result.get(line.getRepoUuid()).get(line.getCommitter());
            }else{
                map = new HashMap<>();
                map.put(ADD,0);
                map.put(DELETE_OTHERS, 0);
                map.put(DELETE_SELF, 0);
                map.put(CHANGE,0);
                if(!result.containsKey(line.getRepoUuid())){
                    Map<String, Map<String, Integer>> developerMap= new HashMap<>();
                    developerMap.put(line.getCommitter(), map);
                    result.put(line.getRepoUuid(), developerMap);
                }else {
                    result.get(line.getRepoUuid()).put(line.getCommitter(), map);
                }
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
            result.get(line.getRepoUuid()).put(line.getCommitter(),map);
        }
        String lastMetaUuid= null;
        String firstCommitter= null;
        for (ValidLineInfo validLineInfo : deleteMap.values()) {
            Map<String,Integer> map;
            //
            if(lastMetaUuid == null || !lastMetaUuid.equals(validLineInfo.getMetaUuid())){
                lastMetaUuid= validLineInfo.getMetaUuid();
                if(firstCommitterMap.get(lastMetaUuid) != null){
                    firstCommitter= firstCommitterMap.get(lastMetaUuid).get("committer");
                }
            }
            if(result.keySet().contains(validLineInfo.getRepoUuid()) && result.get(validLineInfo.getRepoUuid()).containsKey(validLineInfo.getCommitter())){
                map= result.get(validLineInfo.getRepoUuid()).get(validLineInfo.getCommitter());
            }else{
                map = new HashMap<>();
                map.put(ADD,0);
                map.put(DELETE_OTHERS, 0);
                map.put(DELETE_SELF, 0);
                map.put(CHANGE,0);
                if(!result.containsKey(validLineInfo.getRepoUuid())){
                    Map<String, Map<String, Integer>> developerMap= new HashMap<>();
                    developerMap.put(validLineInfo.getCommitter(), map);
                    result.put(validLineInfo.getRepoUuid(), developerMap);
                }else {
                    result.get(validLineInfo.getRepoUuid()).put(validLineInfo.getCommitter(), map);
                }
            }
            if(firstCommitter != null && firstCommitter.equals(validLineInfo.getCommitter())){
                map.put(DELETE_SELF, map.get(DELETE_SELF)+ 1);
            }else{
                map.put(DELETE_OTHERS, map.get(DELETE_OTHERS)+ 1);
            }
            result.get(validLineInfo.getRepoUuid()).put(validLineInfo.getCommitter(),map);
        }
        return result;
    }

    @Override
    public List<JSONObject> getTop5LiveStatements(String repoUuid, String beginDate, String endDate) {
        Map<String,Map<String, Integer>> validLineMap = getValidLineMap(repoUuid, beginDate, endDate, null);
        Map<String, Integer> map= validLineMap.get(repoUuid);
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