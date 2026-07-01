package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

import java.util.List;

public record GuideReferenceEntry(String method, String endpoint, String purpose, String requestType,
		String responseType, String guideScenarioId, List<String> concepts, String exampleCurl) {
}
