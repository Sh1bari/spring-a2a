package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
public class GuideAsyncConfig {

	@Bean
	public TaskExecutor guideTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("guide-");
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(32);
		executor.initialize();
		return executor;
	}

}
