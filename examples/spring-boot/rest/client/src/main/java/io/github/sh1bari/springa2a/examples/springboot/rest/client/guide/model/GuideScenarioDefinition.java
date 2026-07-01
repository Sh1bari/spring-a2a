package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

import java.util.List;

public record GuideScenarioDefinition(String id, int order, String title, String goal, String purpose,
		List<String> learningPoints, String method, String endpoint, List<String> concepts, String defaultPrompt,
		boolean streaming, boolean taskBased, boolean cancellable) {
}
