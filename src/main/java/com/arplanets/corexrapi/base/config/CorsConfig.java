package com.arplanets.corexrapi.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${server.cors.allowed-origins:*}")
    private String[] allowedOrigins;
    @Value("${server.cors.allowed-methods:*}")
    private String[] allowedMethods;
    @Value("${server.cors.allowed-headers:*}")
    private String[] allowedHeaders;
    @Value("${server.cors.allow-credentials:false}")
    private boolean allowCredentials;
    @Value("${server.cors.exposed-headers:*}")
    private String[] exposedHeaders;
    @Value("${server.cors.max-age:0}")
    private int maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允許對所有路徑應用 CORS
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .exposedHeaders(exposedHeaders)
                .maxAge(maxAge); // 預檢請求的有效時間 (秒)
    }
}
