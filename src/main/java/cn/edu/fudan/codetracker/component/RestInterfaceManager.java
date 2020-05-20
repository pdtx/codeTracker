/**
 * @description:
 * @author: fancying
 * @create: 2019-11-02 17:53
 **/
package cn.edu.fudan.codetracker.component;

import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.util.http.fileupload.util.Closeable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RestInterfaceManager {

    @Value("${code.service.path}")
    private String codeServicePath;

    @Value("${commit.service.path}")
    private String commitServicePath;

    @Value("${code.service.repo.path}")
    private String codeServiceRepoPath;

    @Value("${code.service.repo.free.path}")
    private String codeRepoFreePath;

    private RestTemplate restTemplate;

    public RestInterfaceManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getRepoPath(String repoId) {
        return restTemplate.getForObject(codeServicePath + "/" + repoId, JSONObject.class).getJSONObject("data").getString("local_addr");
    }

    public String getLatestCommit(String repoId, String startTime, String endTime) {
        return restTemplate.getForObject(commitServicePath + "?repo_id=" + repoId + "&start_time=" + startTime + "&end_time=" + endTime, JSONObject.class).getJSONArray("data").getJSONObject(0).getString("commit_id");
    }

    public String getCodeServiceRepo(String repoId) {
        return restTemplate.getForObject(codeServiceRepoPath + "?repo_id=" + repoId, JSONObject.class).getJSONObject("data").getString("content");
    }

    public void freeRepo(String repoId, String path) {
        restTemplate.getForObject(codeRepoFreePath + "?repo_id=" + repoId + "&path=" + path,JSONObject.class).getJSONObject("data").getString("status");
    }

}