package cn.edu.fudan.codetracker.domain.projectinfo;

import lombok.Data;

/**
 * @author chen yuan
 */
@Data
public class MethodCall {
    private String uuid;
    /**
     * method field
     */
    private String bodyType;
    /**
     * raw uuid
     */
    private String bodyUuid;
    private String packageName;
    private String className;
    private String signature;
    private String rawMethodUuid;

    public MethodCall() {

    }

    public MethodCall(String uuid, String bodyType, String bodyUuid, String packageName, String className, String signature) {
        this.uuid = uuid;
        this.bodyType = bodyType;
        this.bodyUuid = bodyUuid;
        this.packageName = packageName;
        this.className = className;
        this.signature = signature;
    }
}

