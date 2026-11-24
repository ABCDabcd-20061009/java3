package com.fenglin.springboottest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置：允许前端 Vite 开发服务器（5173）直接访问
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 放行所有来源：覆盖 localhost / 127.0.0.1 / 局域网 IP / 电脑名 / 其他设备访问
                // （配合 allowCredentials 使用 allowedOriginPatterns，不能用 allowedOrigins("*")）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
