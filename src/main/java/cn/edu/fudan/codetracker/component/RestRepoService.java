package cn.edu.fudan.codetracker.component;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-14 19:52
 **/
@Slf4j
@Component
public class RestRepoService extends AbstractRestService{

    @Value("${code.service.path}")
    private String codeServicePath;

    public String getRepoPath(Object repoId) {
        String repo = (String) repoId;
        return super.getRestTemplate().getForObject(codeServicePath + "/" + repo, JSONObject.class).getJSONObject("data").getString("local_addr");
    }


}