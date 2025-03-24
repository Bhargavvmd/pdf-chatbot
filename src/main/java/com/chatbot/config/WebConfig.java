package com.chatbot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Main page
        registry.addViewController("/").setViewName("index");
        
        // API endpoints
        registry.addViewController("/api/pdf/upload").setViewName("index");
        registry.addViewController("/api/pdf/process-directory").setViewName("index");
        registry.addViewController("/api/chat/ask").setViewName("index");
        
        // Error pages
        registry.addViewController("/error").setViewName("error");
    }
} 