package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

import java.util.List;

public record GuideScenarioOutcome(String scenarioId, String title, boolean success, String serverUrl,
		List<GuideCallResult> steps, String summary, List<String> whatHappened, List<String> learningOutcome,
		String currentTaskId, String currentContextId, List<GuideTraceEvent> traceEvents, GuideErrorView error) {
}
