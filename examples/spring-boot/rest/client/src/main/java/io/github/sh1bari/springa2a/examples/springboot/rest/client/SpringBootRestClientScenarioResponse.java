package io.github.sh1bari.springa2a.examples.springboot.rest.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.a2aproject.sdk.spec.AgentCard;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpringBootRestClientScenarioResponse(boolean success, String scenario, String serverUrl,
		AgentCard agentCard, String sentMessage, String receivedMessage, List<String> events, String errorMessage) {

	public static SpringBootRestClientScenarioResponse success(String scenario, String serverUrl, AgentCard agentCard,
			String sentMessage, String receivedMessage, List<String> events) {
		return new SpringBootRestClientScenarioResponse(true, scenario, serverUrl, agentCard, sentMessage,
				receivedMessage, events, null);
	}

	public static SpringBootRestClientScenarioResponse failure(String scenario, String serverUrl, String sentMessage,
			List<String> events, String errorMessage) {
		return new SpringBootRestClientScenarioResponse(false, scenario, serverUrl, null, sentMessage, null, events,
				errorMessage);
	}
}
