package com.pigs.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.pigs.blog.mapper")
@EnableCaching  //开启缓存注解功能
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}