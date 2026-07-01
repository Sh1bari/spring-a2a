package io.github.sh1bari.springa2a.examples.springboot.rest.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpringBootRestClientScenarioResponse(boolean success, String scenario, String serverUrl,
		String sentMessage, String receivedMessage, List<String> events, String errorMessage) {

	public static SpringBootRestClientScenarioResponse success(String scenario, String serverUrl, String sentMessage,
			String receivedMessage, List<String> events) {
		return new SpringBootRestClientScenarioResponse(true, scenario, serverUrl, sentMessage, receivedMessage, events,
				null);
	}

	public static SpringBootRestClientScenarioResponse failure(String scenario, String serverUrl, String sentMessage,
			List<String> events, String errorMessage) {
		return new SpringBootRestClientScenarioResponse(false, scenario, serverUrl, sentMessage, null, events,
				errorMessage);
	}
}
