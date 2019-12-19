package cn.edu.fudan.codetracker.domain.diff;

import com.alibaba.fastjson.JSONObject;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;

/**
 * description:
 * @author fancying
 * create: 2019-12-14 21:07
 **/
@Slf4j
public final class DiffInfo {

    private JSONObject oneDiff;

    private String domainType;
    private String description;
    private String changeRelation;
    private int preBegin = -1;
    private int preEnd = -1;
    private int curBegin = -1;
    private int curEnd = -1;
    private int preParentBegin = -1;
    private int preParentEnd = -1;
    private int curParentBegin = -1;
    private int curParentEnd = -1;

    private String delimiter = "-";

    DiffInfo() {

    }

    public DiffInfo(JSONObject oneDiff) {
        this.oneDiff = oneDiff;
        domainType = oneDiff.getString("type1").toLowerCase();
        description = oneDiff.getString("description");
        changeRelation = oneDiff.getString("type2");
        analyzeRange(oneDiff.getString("range"));
        analyzeParentRange();
    }

    private void analyzeRange(String range) {
        if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(changeRelation)) {

        }
    }

    private void analyzeParentRange() {
        String fatherRangeName = "father-node-range";
        String range ;
        if (! oneDiff.containsKey(fatherRangeName)) {
           return;
        }
        range = oneDiff.getString(fatherRangeName);


    }

    /**
     * getter and setter
     */
    public String getDomainType() {
        return domainType;
    }

    public String getDescription() {
        return description;
    }

    public String getChangeRelation() {
        return changeRelation;
    }

    public int getPreBegin() {
        return preBegin;
    }

    public int getPreEnd() {
        return preEnd;
    }

    public int getCurBegin() {
        return curBegin;
    }

    public int getCurEnd() {
        return curEnd;
    }

    public int getPreParentBegin() {
        return preParentBegin;
    }

    public int getPreParentEnd() {
        return preParentEnd;
    }

    public int getCurParentBegin() {
        return curParentBegin;
    }

    public int getCurParentEnd() {
        return curParentEnd;
    }

}