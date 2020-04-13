package cn.edu.fudan.codetracker.domain.diff;


import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter
@Setter
public final class DiffInfo {
    private String type;
    private Location location;
    private String changeRelation;

    public DiffInfo() {

    }

    //初始化from ClDiff
    public DiffInfo(JSONObject jsonObject) {
        parseDiffInfoFromClDiff(jsonObject);
    }

    public void parseDiffInfoFromClDiff(JSONObject jsonObject) {
        String type = jsonObject.getString("type1");
        switch (type) {
            case "Class" :
                this.type = "class";
                break;
            case "Member" :
                String description = jsonObject.getString("description").toLowerCase();
                if (description.contains("method")) {
                    this.type = "method";
                }
                if (description.contains("field")) {
                    this.type = "field";
                }
                break;
            case "Statement" :
                this.type = "statement";
                break;
            default:
                break;
        }

        this.changeRelation = jsonObject.getString("type2");

        Location location = new Location();
        String range = jsonObject.getString("range");
        if (range.contains("-")) {
            String[] strings = range.split("-");
            if (range.endsWith("-")) {
                setPreLine(strings[0],location);
            } else if (range.startsWith("-")) {
                setCurLine(strings[1],location);
            } else {
                setPreLine(strings[0],location);
                setCurLine(strings[1],location);
            }
        } else {
            if ("Insert".equals(this.changeRelation)) {
                setCurLine(range,location);
            } else if ("Delete".equals(this.changeRelation)) {
                setPreLine(range,location);
            }
        }
        this.location = location;
    }

    public void setPreLine(String preRange, Location location) {
        if (preRange.length() < 5) {
            log.error("preRange incorrect:" + preRange);
            return;
        }
        String str = preRange.substring(1,preRange.length()-1);
        String[] nums = str.split(",");
        location.setPreBegin(Integer.parseInt(nums[0]));
        location.setPreEnd(Integer.parseInt(nums[1]));
    }

    public void setCurLine(String curRange, Location location) {
        if (curRange.length() < 5) {
            log.error("curRange incorrect:" + curRange);
            return;
        }
        String str = curRange.substring(1,curRange.length()-1);
        String[] nums = str.split(",");
        location.setCurBegin(Integer.parseInt(nums[0]));
        location.setCurEnd(Integer.parseInt(nums[1]));
    }


    @Setter
    @Getter
    class Location {
        private int preBegin;
        private int preEnd;
        private int curBegin;
        private int curEnd;
        private String preName;
        private String curName;
        private String preContent;
        private String curContent;

        Location() {

        }

    }

}