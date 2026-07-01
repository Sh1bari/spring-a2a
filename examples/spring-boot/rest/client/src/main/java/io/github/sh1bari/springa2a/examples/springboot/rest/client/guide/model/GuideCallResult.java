package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

import java.util.List;

public record GuideCallResult(String title, String method, String endpoint, String requestJson, String curl,
		String responseType, String humanReadableResponse, String responseJson, String stateBefore, String stateAfter,
		String taskId, String contextId, List<GuideTraceEvent> traceEvents, boolean success, Integer httpStatus,
		GuideErrorView error) {
}
