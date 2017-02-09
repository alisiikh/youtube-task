package com.alisiikh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author lial.
 */
@Configuration
public class AppConfig {

	@Bean
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(20);
		taskExecutor.setMaxPoolSize(50);
		taskExecutor.setThreadGroupName("YouTube-Thread-Group");
		taskExecutor.setThreadNamePrefix("YouTube-Processing-Thread-");
		taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
		return taskExecutor;
	}
}

