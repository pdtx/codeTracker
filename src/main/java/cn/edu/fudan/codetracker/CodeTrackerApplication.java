package cn.edu.fudan.codetracker;

import cn.edu.fudan.codetracker.dao.PackageDao;
import cn.edu.fudan.codetracker.domain.projectinfo.CommonInfo;
import cn.edu.fudan.codetracker.domain.projectinfo.PackageNode;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CodeTrackerApplication {

//    private PackageDao packageDao;
//
//    @Autowired
//    public void setPackageDao(PackageDao packageDao) {
//        this.packageDao = packageDao;
//    }

    public static void main(String[] args) {
        SpringApplication.run(CodeTrackerApplication.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        List<PackageNode> packageNodes = new ArrayList<>();
//        PackageNode packageNode = new PackageNode();
//        packageNode.setUuid(UUID.randomUUID().toString());
//        packageNode.setModuleName("module");
//        packageNode.setPackageName("org.test");
//        packageNode.setVersion(1);
//        packageNodes.add(packageNode);
//        PackageNode packageNode1 = new PackageNode();
//        packageNode1.setUuid(UUID.randomUUID().toString());
//        packageNode1.setModuleName("module1");
//        packageNode1.setPackageName("org.test1");
//        packageNode1.setVersion(1);
//        packageNodes.add(packageNode1);
//
//        CommonInfo commonInfo = new CommonInfo();
//        commonInfo.setStartCommit("11111111");
//        commonInfo.setEndCommit("11111111");
//        try {
//            Date date = dateFormat.parse("2020-02-02 15:00:00");
//            commonInfo.setStartCommitDate(date);
//            commonInfo.setEndCommitDate(date);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        commonInfo.setRepoUuid("22222333333");
//        commonInfo.setBranch("test");
//
//        packageDao.insertPackageInfoList(packageNodes, commonInfo);
//
//    }
}
