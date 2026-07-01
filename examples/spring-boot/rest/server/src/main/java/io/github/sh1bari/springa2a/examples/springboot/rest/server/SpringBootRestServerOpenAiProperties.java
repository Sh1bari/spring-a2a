package io.github.sh1bari.springa2a.examples.springboot.rest.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.ai.openai")
public record SpringBootRestServerOpenAiProperties(String apiKey) {
}
