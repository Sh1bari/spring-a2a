package io.github.sh1bari.springa2a.examples.springboot.rest.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "a2a.example")
public record SpringBootRestClientExampleProperties(String serverUrl, String helloMessage, String streamMessage,
		int streamingTimeoutSeconds) {
}
