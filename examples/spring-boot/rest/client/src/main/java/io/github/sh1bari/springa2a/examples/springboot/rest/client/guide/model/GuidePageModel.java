package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model;

import java.util.List;

public record GuidePageModel(String serverUrl, boolean serverAvailable, String serverStatusMessage,
		String clientStatusMessage, String transport, String a2aSdkVersion, String springBootVersion,
		List<GuidePlaygroundTemplate> playgroundTemplates, String currentTaskId, String currentContextId,
		String callbackUrl) {
}
