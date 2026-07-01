package io.github.sh1bari.springa2a.examples.springboot.rest.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpringBootRestClientDemoOverviewResponse(boolean success, String serverUrl,
		List<String> availableScenarios, List<String> samplePrompts, String note) {

	public static SpringBootRestClientDemoOverviewResponse success(String serverUrl, List<String> availableScenarios,
			List<String> samplePrompts, String note) {
		return new SpringBootRestClientDemoOverviewResponse(true, serverUrl, availableScenarios, samplePrompts, note);
	}

	public static SpringBootRestClientDemoOverviewResponse failure(String serverUrl, String note) {
		return new SpringBootRestClientDemoOverviewResponse(false, serverUrl, List.of(), List.of(), note);
	}

}
