package com.cplan.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * AI Adapter Service application entry point.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AiAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAdapterApplication.class, args);
    }
}
