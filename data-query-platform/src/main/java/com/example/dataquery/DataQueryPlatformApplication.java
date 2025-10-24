package com.example.dataquery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 数据查询平台主应用
 */
@SpringBootApplication(exclude = {QuartzAutoConfiguration.class})
@MapperScan("com.example.dataquery.mapper")
@EnableAsync
public class DataQueryPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataQueryPlatformApplication.class, args);
        System.out.println("========================================");
        System.out.println("数据查询平台启动成功！");
        System.out.println("========================================");
    }
}
