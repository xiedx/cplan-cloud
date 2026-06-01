package com.cplan.creation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Creation Service application entry point.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.cplan.creation.mapper")
public class CreationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreationApplication.class, args);
    }
}
