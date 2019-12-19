package cn.edu.fudan.codetracker.domain.resultmap;

import java.util.List;

//临时接口的result
public class TempMostInfo {
    private String name;
    private int quantity;
    private String uuid;
    private List<TempMostInfo> childInfos;

    public TempMostInfo() {

    }

    public String getName() { return name; }

    public int getQuantity() { return quantity; }

    public List<TempMostInfo> getChildInfos() { return childInfos; }

    public String getUuid() { return uuid; }

    public void setName(String name) { this.name = name; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void setChildInfos(List<TempMostInfo> childInfos) { this.childInfos = childInfos; }

    public void setUuid(String uuid) { this.uuid = uuid; }
}
