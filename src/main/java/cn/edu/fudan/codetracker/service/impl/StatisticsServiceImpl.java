/**
 * @description:
 * @author: fancying
 * @create: 2019-11-11 21:28
 **/
package cn.edu.fudan.codetracker.service.impl;

import cn.edu.fudan.codetracker.dao.*;
import cn.edu.fudan.codetracker.domain.projectinfo.*;
import cn.edu.fudan.codetracker.domain.resultmap.*;
import cn.edu.fudan.codetracker.jgit.JGitHelper;
import cn.edu.fudan.codetracker.service.StatisticsService;
import cn.edu.fudan.codetracker.util.RepoInfoBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private StatisticsDao statisticsDao;
    private Map<String,Integer> committerMap;
    private SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final int addOne = 1;
    private final int addTwo = 2;


    @Override
    public Map<String,Integer> getValidLineCount(String repoUuid, String branch, String beginDate, String endDate) {
        Map<String,Integer> map = new TreeMap<>();
        List<ValidLineInfo> list = new ArrayList<>();
        list.addAll(statisticsDao.getValidLineInfo("class", repoUuid, branch, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo("method", repoUuid, branch, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo("field", repoUuid, branch, beginDate, endDate));
        list.addAll(statisticsDao.getValidLineInfo("statement", repoUuid, branch, beginDate, endDate));
        String lastMetaUuid = "";
        for (ValidLineInfo validInfo: list) {
            if (validInfo.getMetaUuid().equals(lastMetaUuid)) {
                continue;
            }
            if (validInfo.getChangeRelation().equals("DELETE")) {
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
            list.sort(Comparator.comparingLong(Long::longValue));
            Map<String,Double> newMap = new HashMap<>();
            newMap.put("min",(double)list.get(0));
            newMap.put("max",(double)list.get(list.size()-1));
            if (list.size()%2 == 0) {
                Double median = (list.get(list.size()/2) + list.get((list.size()/2)-1)) / 2.0;
                newMap.put("median", new BigDecimal(median).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            } else {
                newMap.put("median",(double)list.get((list.size()-1)/2));
            }
            Long sum = 0L;
            for (Long l : list) {
                sum += l;
            }
            Double average = sum / (list.size() * 1.0);
            newMap.put("average",new BigDecimal(average).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            map.put(key,newMap);
        }
        return map;
    }


    @Override
    public void delete(String repoUuid, String branch) {
        statisticsDao.delete(repoUuid, branch);
    }

    /**
     * getter and setter
     * */
    @Autowired
    public void setStatisticsDao(StatisticsDao statisticsDao) {
        this.statisticsDao = statisticsDao;
    }

}