package cn.edu.fudan.codetracker;

import cn.edu.fudan.codetracker.service.ScanService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 测试可用 {@link CommandLineRunner} or {@link ApplicationRunner}
 * @author fancying
 */
@SpringBootApplication
@EnableAsync
@MapperScan("cn.edu.fudan.codetracker.mapper")
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
