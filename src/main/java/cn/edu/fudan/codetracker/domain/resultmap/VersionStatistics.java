/**
 * @description:
 * @author: fancying
 * @create: 2019-11-11 21:15
 **/
package cn.edu.fudan.codetracker.domain.resultmap;

public class VersionStatistics {

    private int version;
    private int quantity;

    VersionStatistics() {

    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}