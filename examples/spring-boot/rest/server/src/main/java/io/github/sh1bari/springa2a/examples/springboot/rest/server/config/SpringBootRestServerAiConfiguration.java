package io.github.sh1bari.springa2a.examples.springboot.rest.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SpringBootRestServerOpenAiProperties.class)
public class SpringBootRestServerAiConfiguration {

	@Bean
	@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
	public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder.build();
	}

}
