package cn.edu.fudan.codetracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("cn.edu.fudan.codetracker.mapper")
public class CodeTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeTrackerApplication.class, args);
    }

}
