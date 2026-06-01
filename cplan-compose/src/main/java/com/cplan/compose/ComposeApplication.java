package com.cplan.compose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Compose Service application entry point.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ComposeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComposeApplication.class, args);
    }
}
