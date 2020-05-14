package cn.edu.fudan.codetracker.component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * description: rest 调用代理类
 *
 * @author fancying
 * create: 2020-05-14 16:28
 **/
@Slf4j
public class RestInvoker {

    private RestRepoService restRepoService;


    @SneakyThrows
    public Object rest(RestServiceEnum serviceName, String methodName, Object... objects) {
        AbstractRestService abstractRestService = getServiceByName(serviceName);
        Method method = abstractRestService.getClass().getMethod(methodName, objects.getClass());
        return method.invoke(abstractRestService, objects);
    }

    private AbstractRestService getServiceByName(RestServiceEnum serviceName) {
        switch (serviceName) {
            case CODE_SERVICE:
                return restRepoService;
            case PROJECT_SERVICE:
                // TODO 完善后续部分
                return null;
            default:
                log.error("no service");
                return null;
        }
    }

    @Autowired
    public void setRestRepoService(RestRepoService restRepoService) {
        this.restRepoService = restRepoService;
    }

}