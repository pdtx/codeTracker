package cn.edu.fudan.codetracker.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * description:
 * @author fancying
 * create: 2020-5-14 14:12
 **/
abstract class AbstractRestService {

    private RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    RestTemplate getRestTemplate() {
        return restTemplate;
    }
}