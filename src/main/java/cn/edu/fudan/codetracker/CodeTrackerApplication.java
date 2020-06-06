package cn.edu.fudan.codetracker;

import cn.edu.fudan.codetracker.controller.RepoAnalyzerController;
import cn.edu.fudan.codetracker.dao.PackageDao;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.PackageNode;
import cn.edu.fudan.codetracker.service.ScanService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableAsync
@MapperScan("cn.edu.fudan.codetracker.mapper")
//测试用
//public class CodeTrackerApplication implements CommandLineRunner {
public class CodeTrackerApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(CodeTrackerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //testScan();
    }

    private void testScan() {
        String repoUuid = "dubbo";
        String branch = "master";
        String begin = "02eabd6369400fd61db29ca44492678af745bccd";
        scanService.scan(repoUuid, branch, begin);
    }

    @Autowired
    public void setScanService(ScanService scanService) {
        this.scanService = scanService;
    }

    private ScanService scanService;
}
