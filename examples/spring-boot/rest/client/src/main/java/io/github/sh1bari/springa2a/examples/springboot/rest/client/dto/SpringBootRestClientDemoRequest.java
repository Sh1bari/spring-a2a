package io.github.sh1bari.springa2a.examples.springboot.rest.client.dto;

import org.jspecify.annotations.Nullable;

public record SpringBootRestClientDemoRequest(@Nullable String helloMessage, @Nullable String streamMessage,
		@Nullable Integer streamingTimeoutSeconds) {
}
