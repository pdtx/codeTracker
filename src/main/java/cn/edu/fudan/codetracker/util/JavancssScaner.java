package cn.edu.fudan.codetracker.util;

import com.alibaba.fastjson.JSONObject;
import javancss.FunctionMetric;
import javancss.Javancss;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavancssScaner {
    public static int scanFile(String repoPath) {
        int sum = 0;
        File projectDir = new File(repoPath);
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith(".java"),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(projectDir);
        for (String path: pathList) {
            File tempFile = new File(path);
            Javancss javancss = new Javancss(tempFile);
            int add = javancss.getNcss();
            sum += add;
        }
        return sum;
    }

    public static int scanOneFile(String path) {
        File tempFile = new File(path);
        Javancss javancss = new Javancss(tempFile);
        return javancss.getNcss();
    }

    public static Map<String,Integer> getOneFileCCNs(String filePath) {
        File file = new File(filePath);
        Javancss javancss = new Javancss(file);
        Map<String,Integer> methodCCNs = new HashMap<>(3);
        List<FunctionMetric> functionMetrics = javancss.getFunctionMetrics();
        for (FunctionMetric functionMetric : functionMetrics) {
            String signature = getSignature(functionMetric.name);
            methodCCNs.put(signature,functionMetric.ccn);
        }
        return methodCCNs;
    }

    private static String getSignature(String name) {
        String[] nameWords = name.split("\\(");
        String[] prefixWords = nameWords[0].split("\\.");
        String signature = prefixWords[prefixWords.length-1] + "(" + nameWords[nameWords.length-1];
        return signature;
    }

    public static void main(String[] args) {
        Map<String,Integer> jsonObject = getOneFileCCNs("/Users/tangyuan/Documents/Gitlab/codeTracker/src/main/java/cn/edu/fudan/codetracker/service/impl/ScanServiceImpl.java");
        System.out.println(jsonObject);

    }
}
