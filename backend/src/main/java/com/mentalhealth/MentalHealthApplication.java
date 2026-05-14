package com.mentalhealth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 心理健康辅助干预平台 - 启动类
 * 作用：Spring Boot 项目入口，启动内嵌 Tomcat 并自动装配所有组件
 */
@SpringBootApplication
@MapperScan("com.mentalhealth.mapper")
public class MentalHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentalHealthApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  心理健康辅助干预平台启动成功！");
        System.out.println("  后端地址: http://localhost:8080");
        System.out.println("==========================================");
    }
}
