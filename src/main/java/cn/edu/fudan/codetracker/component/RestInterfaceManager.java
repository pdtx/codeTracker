/**
 * @description:
 * @author: fancying
 * @create: 2019-11-02 17:53
 **/
package cn.edu.fudan.codetracker.component;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestInterfaceManager {

    @Value("${code.service.path}")
    private String codeServicePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getRepoPath(String repoId) {
        return restTemplate.getForObject(codeServicePath + "/" + repoId, JSONObject.class).getJSONObject("data").getString("local_addr");
    }

}