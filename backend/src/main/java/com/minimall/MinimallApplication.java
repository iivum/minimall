package com.minimall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.minimall.domain.mapper")
public class MinimallApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinimallApplication.class, args);
    }
}
