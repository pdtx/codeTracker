package cn.edu.fudan.codetracker.core;

import cn.edu.fudan.codetracker.domain.diff.DiffInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.BaseNode;
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
 * description: 增加的文件映射
 *
 * @author fancying
 * create: 2020-03-20 19:27
 **/
@Slf4j
public class LogicalChangedHandler implements NodeMapping {

    private String diffPath;

    private Map<String, List<DiffInfo>> map;

    private LogicalChangedHandler(){}

    public static LogicalChangedHandler getInstance(){
        return MappingGeneratorHolder.LOGICAL_CHANGED_HANDLER;
    }

    private static final class MappingGeneratorHolder {
        private static final LogicalChangedHandler LOGICAL_CHANGED_HANDLER = new LogicalChangedHandler();
    }

    @Override
    public void subTreeMapping(BaseNode preRoot, BaseNode curRoot) {
        extractFromDiff();
    }

    public void extractFromDiff() {
        map = new HashMap<>();
        map.put("class",new ArrayList<>());
        map.put("method",new ArrayList<>());
        map.put("field",new ArrayList<>());
        map.put("statement",new ArrayList<>());
        String input;
        try {
            input = FileUtils.readFileToString(new File(diffPath), "UTF-8");
        }catch (IOException e) {
            log.error(e.getMessage());
            return;
        }
        JSONArray diffDetail = JSONArray.parseArray(input);
        for (int i = 0; i < diffDetail.size() ; i++) {
            JSONObject jsonObject = diffDetail.getJSONObject(i);
            DiffInfo diffInfo = new DiffInfo(jsonObject);
            List<DiffInfo> list = map.get(diffInfo.getType());
            list.add(diffInfo);
            map.put(diffInfo.getType(),list);
        }
    }

    public void setDiffPath(String diffPath) {
        this.diffPath = diffPath;
    }
}