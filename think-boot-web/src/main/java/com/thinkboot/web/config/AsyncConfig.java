package com.thinkboot.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 *
 * 提供操作日志异步落库所需的 logExecutor 线程池，
 * 并开启 @EnableAsync 使 @Async("logExecutor") 注解生效。
 * 重复声明 @EnableAsync 与用户自定义配置不冲突；
 * 用户也可自定义同名 logExecutor Bean 覆盖默认线程池参数。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("logExecutor")
    @ConditionalOnMissingBean(name = "logExecutor")
    public ThreadPoolTaskExecutor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("log-executor-");
        // 队列满时丢弃新任务：操作日志属于非核心链路，不能阻塞业务线程
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}
