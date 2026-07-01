package io.github.sh1bari.springa2a.examples.springboot.rest.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.a2a.example")
public record SpringBootRestClientExampleProperties(String serverUrl, String helloMessage, String streamMessage,
		int streamingTimeoutSeconds) {
}
