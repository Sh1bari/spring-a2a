package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.scenario;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.config.SpringBootRestClientExampleProperties;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuidePlaygroundTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GuidePlaygroundTemplateCatalog {

	private final SpringBootRestClientExampleProperties properties;

	private final List<GuidePlaygroundTemplate> templates;

	public GuidePlaygroundTemplateCatalog(SpringBootRestClientExampleProperties properties) {
		this.properties = properties;
		this.templates = List.of(new GuidePlaygroundTemplate("discover-agent", 1, "Discover Agent",
				"Read the public Agent Card to learn what the server can do before sending any other request.", "GET",
				"/.well-known/agent-card.json", "{\n  \"note\": \"Discovery does not need a body\"\n}", false, false),
				new GuidePlaygroundTemplate("send-message", 2, "Send Message",
						"Send a normal user prompt and inspect the immediate reply or task response.", "POST",
						"/message:send", "{\n  \"prompt\": \"hello from the playground\"\n}", false, false),
				new GuidePlaygroundTemplate("create-and-inspect-task", 3, "Create and Inspect Task",
						"Create a predictable task first, then fetch the same task by id.", "POST", "/message:send",
						"{\n  \"prompt\": \"/task\"\n}", false, false),
				new GuidePlaygroundTemplate("inspect-task", 4, "Inspect Task",
						"Fetch an existing task by id and inspect its current state and history.", "GET",
						"/tasks/{taskId}", "{\n  \"taskId\": \"\"\n}", false, true),
				new GuidePlaygroundTemplate("list-tasks", 5, "List Tasks",
						"Ask the server for the tasks it currently knows about, optionally filtered by context or status.",
						"GET", "/tasks",
						"{\n  \"contextId\": \"\",\n  \"status\": \"\",\n  \"pageSize\": 10,\n  \"pageToken\": \"\",\n  \"historyLength\": 2,\n  \"includeArtifacts\": true\n}",
						false, false),
				new GuidePlaygroundTemplate("stream-task-updates", 6, "Stream Task Updates",
						"Open the emitter and watch task status and artifact events arrive in real time.", "POST",
						"/message:stream", "{\n  \"prompt\": \"stream this\",\n  \"streamingTimeoutSeconds\": 20\n}",
						true, false),
				new GuidePlaygroundTemplate("subscribe-task", 7, "Subscribe to Task",
						"Attach a server subscription to an existing task so the client can receive future updates.",
						"POST", "/tasks/{taskId}:subscribe", "{\n  \"taskId\": \"\"\n}", false, true),
				new GuidePlaygroundTemplate("cancel-task", 8, "Cancel Task",
						"Cancel a long-running task and then inspect the final state again.", "POST",
						"/tasks/{taskId}:cancel", "{\n  \"taskId\": \"\"\n}", false, true),
				new GuidePlaygroundTemplate("create-push-config", 9, "Create Push Notification Config",
						"Register a push notification callback URL for a task so the server can call back later.",
						"POST", "/tasks/{taskId}/pushNotificationConfigs",
						"{\n  \"taskId\": \"\",\n  \"url\": \"" + properties.callbackUrl()
								+ "\",\n  \"token\": \"demo-token\",\n  \"authentication\": {\n    \"scheme\": \"Bearer\",\n    \"credentials\": \"secret\"\n  }\n}",
						false, true),
				new GuidePlaygroundTemplate("get-push-config", 10, "Get Push Notification Config",
						"Fetch one saved push notification configuration by config id.", "GET",
						"/tasks/{taskId}/pushNotificationConfigs/{configId}",
						"{\n  \"taskId\": \"\",\n  \"configId\": \"\"\n}", false, true),
				new GuidePlaygroundTemplate("list-push-configs", 11, "List Push Notification Configs",
						"List the push notification callbacks registered for a task.", "GET",
						"/tasks/{taskId}/pushNotificationConfigs",
						"{\n  \"taskId\": \"\",\n  \"pageSize\": 10,\n  \"pageToken\": \"\"\n}", false, true),
				new GuidePlaygroundTemplate("delete-push-config", 12, "Delete Push Notification Config",
						"Remove a push notification configuration from a task.", "DELETE",
						"/tasks/{taskId}/pushNotificationConfigs/{configId}",
						"{\n  \"taskId\": \"\",\n  \"configId\": \"\"\n}", false, true),
				new GuidePlaygroundTemplate("callback-demo", 13, "Push Callback Demo",
						"Run a 20 second streaming task and let the client register a push callback while it is still running.",
						"POST", "/message:stream", "{\n  \"prompt\": \"/callback-demo\"\n}", true, false));
	}

	public List<GuidePlaygroundTemplate> templates() {
		return this.templates;
	}

	public GuidePlaygroundTemplate findTemplate(String id) {
		return this.templates.stream()
			.filter(template -> template.id().equals(id))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown playground template: " + id));
	}

}
