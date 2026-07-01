package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

public record GuideScenarioRequest(String prompt, Integer streamingTimeoutSeconds, String taskId, Integer historyLength,
		String contextId, String status, Integer pageSize, String pageToken, Boolean includeArtifacts, String configId,
		String callbackUrl) {
}
