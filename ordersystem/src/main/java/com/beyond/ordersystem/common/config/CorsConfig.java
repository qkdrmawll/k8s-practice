package com.beyond.ordersystem.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://www.qkdrmawll.store") //허용 url 명시 // 뷰에서 쓸 url
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
