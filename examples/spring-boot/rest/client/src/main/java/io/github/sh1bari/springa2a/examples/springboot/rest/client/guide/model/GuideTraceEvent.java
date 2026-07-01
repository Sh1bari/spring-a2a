package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

import java.time.Instant;

public record GuideTraceEvent(Instant timestamp, String stage, String description, String rawJson) {
}
