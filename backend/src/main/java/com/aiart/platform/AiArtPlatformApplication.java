package com.aiart.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aiart.platform.mapper")
public class AiArtPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiArtPlatformApplication.class, args);
    }
}
