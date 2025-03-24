package com.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "pdfProcessingExecutor")
    public Executor pdfProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // Number of core threads
        executor.setMaxPoolSize(8);  // Maximum number of threads
        executor.setQueueCapacity(100); // Queue capacity for tasks
        executor.setThreadNamePrefix("pdf-processing-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "chatExecutor")
    public Executor chatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Number of core threads for chat operations
        executor.setMaxPoolSize(4);  // Maximum number of threads for chat
        executor.setQueueCapacity(50); // Queue capacity for chat tasks
        executor.setThreadNamePrefix("chat-processing-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "vectorStoreExecutor")
    public Executor vectorStoreExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Number of core threads for vector store operations
        executor.setMaxPoolSize(4);  // Maximum threads for vector store
        executor.setQueueCapacity(50); // Queue capacity for vector store tasks
        executor.setThreadNamePrefix("vector-store-");
        executor.initialize();
        return executor;
    }
} 