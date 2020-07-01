package cn.edu.fudan.codetracker.domain.projectinfo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * description: 方法调用关系
 *
 * @author fancying
 * create: 2020-06-18 17:28
 **/
@Data
@AllArgsConstructor
public class MethodCallRelationship {

    private String packageName;
    private String className;
    private String signature;

    @Override
    public int hashCode() {
        return (className + packageName + signature).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if(o == null){
            return false;
        }

        if(o instanceof MethodCallRelationship){
            MethodCallRelationship methodCallRelationship = (MethodCallRelationship)o;
            return this.signature.equals(methodCallRelationship.getSignature()) &&
                    this.packageName.equals(methodCallRelationship.getPackageName()) &&
                    this.className.equals(methodCallRelationship.getClassName());
        }
        return false;
    }

}