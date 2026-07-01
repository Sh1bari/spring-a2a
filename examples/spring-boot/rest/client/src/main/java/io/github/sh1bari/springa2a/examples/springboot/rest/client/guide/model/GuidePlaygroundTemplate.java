package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

public record GuidePlaygroundTemplate(String id, int order, String title, String description, String method,
		String endpoint, String defaultRequestJson, boolean stream, boolean requiresTaskId) {
}
