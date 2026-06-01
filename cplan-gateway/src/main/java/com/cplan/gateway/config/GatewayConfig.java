package com.cplan.gateway.config;

import com.cplan.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean configuration for Gateway-specific components.
 */
@Configuration
public class GatewayConfig {

    @Value("${cplan.jwt.secret:cplan-default-secret-key-change-in-production-32chars-min}")
    private String jwtSecret;

    @Value("${cplan.jwt.expire-seconds:86400}")
    private long jwtExpireSeconds;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret, jwtExpireSeconds);
    }
}
