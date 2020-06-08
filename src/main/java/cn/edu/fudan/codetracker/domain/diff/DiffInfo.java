package cn.edu.fudan.codetracker.domain.diff;


import cn.edu.fudan.codetracker.domain.projectinfo.*;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;


/**
 * 记录每一个变更信息
 * @author 汤圆
 */
@Slf4j
@Data
public final class DiffInfo {
    private String type;
    private Location location;
    private String changeRelation;
    private JSONObject jsonObject;
    private String description;

    /**
     * 初始化from ClDiff
     */
    public DiffInfo(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
        parseDiffInfoFromClDiff(jsonObject);
    }

    public void parseDiffInfoFromClDiff(JSONObject jsonObject) {
        String type = jsonObject.getString("type1");
        switch (type) {
            case "ClassOrInterface" :
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
        this.description = jsonObject.getString("description");

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

    public BaseNode findChangeNode(Set<BaseNode> set, boolean isCur) {
        if (set == null || set.size() == 0) {
            return null;
        }
        int begin,end;
        if (isCur) {
            begin = location.getCurBegin();
            end = location.getCurEnd();
        } else {
            begin = location.getPreBegin();
            end = location.getPreEnd();
        }
        if (begin == 0 && end == 0) {
            return null;
        }
        boolean isFirst = true;
        String type = "";
        for (BaseNode baseNode: set) {
            if (isFirst) {
                type = baseNode.getProjectInfoLevel().getName();
                isFirst = false;
            }
            int nodeBegin = -1,nodeEnd = -1;

            switch (type) {
                case "class":
                    ClassNode classNode = (ClassNode)baseNode;
                    nodeBegin = classNode.getBegin();
                    nodeEnd = classNode.getEnd();
                    break;
                case "method":
                    MethodNode methodNode = (MethodNode)baseNode;
                    nodeBegin = methodNode.getBegin();
                    nodeEnd = methodNode.getEnd();
                    break;
                case "field":
                    FieldNode fieldNode = (FieldNode)baseNode;
                    nodeBegin = fieldNode.getBegin();
                    nodeEnd = fieldNode.getEnd();
                    break;
                case "statement":
                    StatementNode statementNode = (StatementNode)baseNode;
                    nodeBegin = statementNode.getBegin();
                    nodeEnd = statementNode.getEnd();
                    break;
                default:
                    break;
            }

            if (isMatch(begin, end, nodeBegin, nodeEnd)) {
                return baseNode;
            }
        }
        return null;
    }


    private boolean isMatch(int diffBegin, int diffEnd, int nodeBegin, int nodeEnd) {
        return (diffBegin == nodeBegin) && (diffEnd == nodeEnd);
    }



    @Data
    @NoArgsConstructor
    class Location {
        private int preBegin;
        private int preEnd;
        private int curBegin;
        private int curEnd;
        private String preName;
        private String curName;
        private String preContent;
        private String curContent;

    }

}