package cn.edu.fudan.codetracker.domain.diff;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 将特定工具的输入 转换成 定义好的 Logical Diff
 *  后续会抽取成类的形式
 *
 * @author fancying
 * create: 2020-06-07 18:05
 **/
@Slf4j
public class CldiffAdapter {

    /**
     * todo 提取出来 解耦操作
     */
    public static Map<String, List<DiffInfo>> extractFromDiff(String diffPath) {
        Map<String, List<DiffInfo>> map = new HashMap<>(8);
        String input;
        try {
            input = FileUtils.readFileToString(new File(diffPath), "UTF-8");
        }catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
        map.put("class", new ArrayList<>());
        map.put("method", new ArrayList<>());
        map.put("field", new ArrayList<>());
        map.put("statement", new ArrayList<>());
        JSONArray diffDetail = JSONArray.parseArray(input);
        for (int i = 0; i < diffDetail.size() ; i++) {
            JSONObject jsonObject = diffDetail.getJSONObject(i);
            DiffInfo diffInfo = new DiffInfo(jsonObject);
            if (diffInfo.getType() == null) {
                log.error("diff info type error : " + jsonObject.getString("type1") + " ; " + jsonObject.getString("description"));
                continue;
            }
            List<DiffInfo> list = map.get(diffInfo.getType());
            list.add(diffInfo);
            map.put(diffInfo.getType(), list);
        }
        return map;
    }
}