package io.github.sh1bari.springa2a.examples.springboot.rest.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpringBootRestClientFullFlowResponse(boolean success, String serverUrl,
		SpringBootRestClientScenarioResponse blocking, SpringBootRestClientScenarioResponse streaming,
		SpringBootRestClientScenarioResponse help, String errorMessage) {

	public static SpringBootRestClientFullFlowResponse success(String serverUrl,
			SpringBootRestClientScenarioResponse blocking, SpringBootRestClientScenarioResponse streaming,
			SpringBootRestClientScenarioResponse help) {
		return new SpringBootRestClientFullFlowResponse(true, serverUrl, blocking, streaming, help, null);
	}

	public static SpringBootRestClientFullFlowResponse failure(String serverUrl, String errorMessage) {
		return new SpringBootRestClientFullFlowResponse(false, serverUrl, null, null, null, errorMessage);
	}
}
