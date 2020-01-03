package cn.edu.fudan.codetracker.domain.diff;

import cn.edu.fudan.codetracker.domain.ProjectInfoLevel;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * description:
 * @author fancying
 * create: 2019-12-14 21:07
 **/
@Slf4j
public final class DiffInfo {

    private Map<ProjectInfoLevel, List<OneDiff>> diffInfo;

    DiffInfo() {

    }

    public DiffInfo(JSONArray diffDetail) {
        diffInfo = new HashMap<>(4);
        diffInfo.put(ProjectInfoLevel.CLASS, new ArrayList<>());
        diffInfo.put(ProjectInfoLevel.METHOD, new ArrayList<>());
        diffInfo.put(ProjectInfoLevel.FIELD, new ArrayList<>());
        diffInfo.put(ProjectInfoLevel.STATEMENT, new ArrayList<>());
        for (int j = 0; j < diffDetail.size(); j++) {
            JSONObject oneDiff = diffDetail.getJSONObject(j);
            String domainType = oneDiff.getString("type1").toLowerCase();
            String description = oneDiff.getString("description");
            if (("statement").equals(domainType)) {
                diffInfo.get(ProjectInfoLevel.STATEMENT).add(new OneDiff(oneDiff));
                continue;
            }
            // method
            if ("member".equals(domainType) && description.toLowerCase().contains("method")) {
                diffInfo.get(ProjectInfoLevel.METHOD).add(new OneDiff(oneDiff));
                continue;
            }
            if ("member".equals(domainType) && description.toLowerCase().contains("field")) {
                diffInfo.get(ProjectInfoLevel.FIELD).add(new OneDiff(oneDiff));
                continue;
            }
            if (("classorinterface").equals(domainType)) {
                diffInfo.get(ProjectInfoLevel.CLASS).add(new OneDiff(oneDiff));
            }
        }
        diffInfo.get(ProjectInfoLevel.STATEMENT).sort(Comparator.comparing(OneDiff::getBeginLine));
    }

    public Map<ProjectInfoLevel, List<OneDiff>> getDiffInfo() {
        return diffInfo;
    }

    public class OneDiff {
        private String description;
        private String changeRelation;
        private String range;
        private String parentRange;

        private int beginLine;
//        private int preBegin = -1;
//        private int preEnd = -1;
//        private int curBegin = -1;
//        private int curEnd = -1;
//        private int preParentBegin = -1;
//        private int preParentEnd = -1;
//        private int curParentBegin = -1;
//        private int curParentEnd = -1;

        private String delimiter = "-";

        OneDiff(JSONObject oneDiff) {
            description = oneDiff.getString("description");
            changeRelation = oneDiff.getString("type2");
            parentRange = "";
            if (oneDiff.containsKey("father-node-range")) {
                parentRange = oneDiff.getString("father-node-range");
            }
            range = oneDiff.getString("range");
            beginLine = Integer.valueOf(range.substring(range.indexOf('(') + 1, range.indexOf(',')));
//            analyzeRange();
//            analyzeParentRange();
        }


        private void analyzeRange() {
            if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(changeRelation)) {

            }
        }

        private void analyzeParentRange() {
            String fatherRangeName = "father-node-range";

        }

        /**
         * getter and setter
         */

        public String getDescription() {
            return description;
        }

        public String getChangeRelation() {
            return changeRelation;
        }

        public String getRange() {
            return range;
        }

        public String getParentRange() {
            return parentRange;
        }

        int getBeginLine() {
            return beginLine;
        }
//        public int getPreBegin() {
//            return preBegin;
//        }
//
//        public int getPreEnd() {
//            return preEnd;
//        }
//
//        public int getCurBegin() {
//            return curBegin;
//        }
//
//        public int getCurEnd() {
//            return curEnd;
//        }
//
//        public int getPreParentBegin() {
//            return preParentBegin;
//        }
//
//        public int getPreParentEnd() {
//            return preParentEnd;
//        }
//
//        public int getCurParentBegin() {
//            return curParentBegin;
//        }
//
//        public int getCurParentEnd() {
//            return curParentEnd;
//        }

    }

}