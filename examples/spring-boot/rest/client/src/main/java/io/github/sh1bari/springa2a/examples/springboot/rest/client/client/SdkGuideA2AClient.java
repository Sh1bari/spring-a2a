package io.github.sh1bari.springa2a.examples.springboot.rest.client.client;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.config.SpringBootRestClientExampleProperties;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.example.GuideMessageA2AClient;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideCallResult;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideTraceEvent;
import lombok.RequiredArgsConstructor;
import org.a2aproject.sdk.A2A;
import org.a2aproject.sdk.client.Client;
import org.a2aproject.sdk.client.ClientEvent;
import org.a2aproject.sdk.client.MessageEvent;
import org.a2aproject.sdk.client.TaskEvent;
import org.a2aproject.sdk.client.TaskUpdateEvent;
import org.a2aproject.sdk.client.config.ClientConfig;
import org.a2aproject.sdk.client.transport.rest.RestTransport;
import org.a2aproject.sdk.client.transport.rest.RestTransportConfigBuilder;
import org.a2aproject.sdk.jsonrpc.common.json.JsonProcessingException;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.CancelTaskParams;
import org.a2aproject.sdk.spec.ListTasksParams;
import org.a2aproject.sdk.spec.ListTaskPushNotificationConfigsResult;
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.TaskPushNotificationConfig;
import org.a2aproject.sdk.spec.Task;
import org.a2aproject.sdk.spec.TaskQueryParams;
import org.a2aproject.sdk.spec.TaskState;
import org.a2aproject.sdk.spec.TextPart;
import org.springframework.util.ReflectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class SdkGuideA2AClient implements GuideA2AClient {

	private final SpringBootRestClientExampleProperties properties;

	private final GuideMessageA2AClient guideMessageA2AClient;

	@Override
	public GuideCallResult discoverAgent() throws Exception {
		AgentCard agentCard = A2A.getAgentCard(this.properties.serverUrl());
		return toCallResult("Discover Agent", "GET", "/.well-known/agent-card.json", null, null, 200, agentCard,
				List.<GuideTraceEvent>of(), null, null, null, null);
	}

	@Override
	public GuideCallResult sendMessage(AgentCard agentCard, String prompt) throws Exception {
		return this.guideMessageA2AClient.sendMessage(agentCard, prompt);
	}

	@Override
	public GuideCallResult inspectTask(AgentCard agentCard, String taskId, Integer historyLength) throws Exception {
		Task task = null;
		try (Client client = new ExchangeContext(agentCard, false).client()) {
			task = client.getTask(new TaskQueryParams(taskId, historyLength), null);
		}
		return toCallResult("Inspect Task", "GET", "/tasks/" + taskId, null,
				"curl \"" + this.properties.serverUrl() + "/tasks/" + taskId + "?historyLength="
						+ (historyLength == null ? 0 : historyLength) + "\"",
				200, task, List.<GuideTraceEvent>of(), null, null, null, null);
	}

	@Override
	public GuideCallResult streamMessage(AgentCard agentCard, String prompt, int timeoutSeconds,
			java.util.function.Consumer<GuideTraceEvent> traceConsumer) throws Exception {
		return this.guideMessageA2AClient.streamMessage(agentCard, prompt, timeoutSeconds, traceConsumer);
	}

	@Override
	public GuideCallResult listTasks(AgentCard agentCard, String contextId, String status, Integer pageSize,
			String pageToken, Integer historyLength, Boolean includeArtifacts) throws Exception {
		Object result;
		try (Client client = new ExchangeContext(agentCard, false).client()) {
			ListTasksParams.Builder builder = ListTasksParams.builder()
				.contextId(contextId)
				.pageSize(pageSize)
				.pageToken(pageToken)
				.historyLength(historyLength)
				.includeArtifacts(includeArtifacts);
			if (status != null && !status.isBlank()) {
				builder.status(parseTaskState(status));
			}
			result = client.listTasks(builder.build(), null);
		}
		java.util.LinkedHashMap<String, Object> request = new java.util.LinkedHashMap<>();
		if (contextId != null && !contextId.isBlank()) {
			request.put("contextId", contextId);
		}
		if (status != null && !status.isBlank()) {
			request.put("status", status);
		}
		if (pageSize != null) {
			request.put("pageSize", pageSize);
		}
		if (pageToken != null && !pageToken.isBlank()) {
			request.put("pageToken", pageToken);
		}
		if (historyLength != null) {
			request.put("historyLength", historyLength);
		}
		if (includeArtifacts != null) {
			request.put("includeArtifacts", includeArtifacts);
		}
		String requestJson = request.isEmpty() ? null : toJson(request);
		return toCallResult("List Tasks", "GET", "/tasks", requestJson, null, 200, result, List.<GuideTraceEvent>of(),
				null, null, null, null);
	}

	@Override
	public GuideCallResult cancelTask(AgentCard agentCard, String taskId) throws Exception {
		Task task;
		try (Client client = new ExchangeContext(agentCard, false).client()) {
			task = client.cancelTask(new CancelTaskParams(taskId), null);
		}
		return toCallResult("Cancel Task", "POST", "/tasks/" + taskId + ":cancel", null,
				"curl -X POST \"" + this.properties.serverUrl() + "/tasks/" + taskId + ":cancel\"", 200, task,
				List.<GuideTraceEvent>of(), null, null, task.id(), task.contextId());
	}

	@Override
	public List<GuideCallResult> subscribeToTask(AgentCard agentCard, String taskId) throws Exception {
		List<GuideTraceEvent> trace = new ArrayList<>();
		AtomicReference<Task> lastTask = new AtomicReference<>();
		String requestJson = toJson(java.util.Map.of("taskId", taskId));
		HttpRequest request = HttpRequest
			.newBuilder(URI.create(this.properties.serverUrl() + "/tasks/" + taskId + ":subscribe"))
			.header("A2A-Version", resolveProtocolVersion(agentCard))
			.header("Accept", "text/event-stream")
			.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.POST(HttpRequest.BodyPublishers.ofString(requestJson))
			.build();
		HttpResponse<java.io.InputStream> response = HttpClient.newHttpClient()
			.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			String body = new String(response.body().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			throw new IllegalStateException("Subscribe request failed with HTTP " + response.statusCode()
					+ (body.isBlank() ? "" : ": " + body));
		}
		try (java.io.BufferedReader reader = new java.io.BufferedReader(
				new java.io.InputStreamReader(response.body(), java.nio.charset.StandardCharsets.UTF_8))) {
			StringBuilder data = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					if (!data.isEmpty()) {
						String rawJson = data.toString();
						GuideTraceEvent event = describeSseEvent(rawJson);
						trace.add(event);
						Task parsedTask = extractTask(rawJson);
						if (parsedTask != null) {
							lastTask.set(parsedTask);
						}
						data.setLength(0);
					}
					continue;
				}
				if (line.startsWith("data:")) {
					if (!data.isEmpty()) {
						data.append('\n');
					}
					data.append(line.substring("data:".length()).trim());
				}
			}
			if (!data.isEmpty()) {
				String rawJson = data.toString();
				GuideTraceEvent event = describeSseEvent(rawJson);
				trace.add(event);
				Task parsedTask = extractTask(rawJson);
				if (parsedTask != null) {
					lastTask.set(parsedTask);
				}
			}
		}
		return List.of(toCallResult("Subscribe to Task", "POST", "/tasks/" + taskId + ":subscribe", requestJson,
				"curl -X POST \"" + this.properties.serverUrl() + "/tasks/" + taskId
						+ ":subscribe\" -H \"Content-Type: application/json\" -d '" + requestJson + "'",
				200, lastTask.get(), trace, null, null, null, null));
	}

	@Override
	public GuideCallResult createTaskPushNotificationConfig(AgentCard agentCard, String taskId, String callbackUrl)
			throws Exception {
		java.util.LinkedHashMap<String, Object> request = new java.util.LinkedHashMap<>();
		request.put("taskId", taskId);
		request.put("url", callbackUrl);
		request.put("token", "demo-token");
		java.util.LinkedHashMap<String, Object> authentication = new java.util.LinkedHashMap<>();
		authentication.put("scheme", "Bearer");
		authentication.put("credentials", "secret");
		request.put("authentication", authentication);
		String responseBody = sendPushNotificationConfigRequest(agentCard,
				this.properties.serverUrl() + "/tasks/" + taskId + "/pushNotificationConfigs", "POST", toJson(request));
		TaskPushNotificationConfig config = JsonUtil.fromJson(responseBody, TaskPushNotificationConfig.class);
		return toCallResult("Create Push Config", "POST", "/tasks/" + taskId + "/pushNotificationConfigs",
				toJson(request), null, 200, config, List.<GuideTraceEvent>of(), null, null, taskId, null);
	}

	@Override
	public GuideCallResult getTaskPushNotificationConfig(AgentCard agentCard, String taskId, String configId)
			throws Exception {
		String responseBody = sendPushNotificationConfigRequest(agentCard,
				this.properties.serverUrl() + "/tasks/" + taskId + "/pushNotificationConfigs/" + configId, "GET", null);
		TaskPushNotificationConfig config = JsonUtil.fromJson(responseBody, TaskPushNotificationConfig.class);
		return toCallResult("Get Push Config", "GET", "/tasks/" + taskId + "/pushNotificationConfigs/" + configId, null,
				null, 200, config, List.<GuideTraceEvent>of(), null, null, taskId, null);
	}

	@Override
	public GuideCallResult listTaskPushNotificationConfigs(AgentCard agentCard, String taskId, Integer pageSize,
			String pageToken) throws Exception {
		StringBuilder uri = new StringBuilder(
				this.properties.serverUrl() + "/tasks/" + taskId + "/pushNotificationConfigs");
		boolean hasQuery = false;
		if (pageSize != null) {
			uri.append(hasQuery ? '&' : '?').append("pageSize=").append(pageSize);
			hasQuery = true;
		}
		if (pageToken != null && !pageToken.isBlank()) {
			uri.append(hasQuery ? '&' : '?').append("pageToken=").append(pageToken);
		}
		String responseBody = sendPushNotificationConfigRequest(agentCard, uri.toString(), "GET", null);
		Object result = JsonUtil.fromJson(responseBody, ListTaskPushNotificationConfigsResult.class);
		return toCallResult("List Push Configs", "GET", "/tasks/" + taskId + "/pushNotificationConfigs", null, null,
				200, result, List.<GuideTraceEvent>of(), null, null, taskId, null);
	}

	@Override
	public GuideCallResult deleteTaskPushNotificationConfig(AgentCard agentCard, String taskId, String configId)
			throws Exception {
		sendPushNotificationConfigRequest(agentCard,
				this.properties.serverUrl() + "/tasks/" + taskId + "/pushNotificationConfigs/" + configId, "DELETE",
				null);
		return toCallResult("Delete Push Config", "DELETE", "/tasks/" + taskId + "/pushNotificationConfigs/" + configId,
				null, null, 204, null, List.<GuideTraceEvent>of(), null, null, taskId, null);
	}

	private GuideCallResult toCallResult(String title, String method, String endpoint, String requestJson,
			String curlOverride, int httpStatus, Object response, List<GuideTraceEvent> trace, String stateBefore,
			String stateAfter, String taskId, String contextId) {
		String responseJson = response == null ? null : toJson(response);
		String humanReadable = humanReadable(response);
		String responseType = responseType(response);
		String effectiveTaskId = taskId;
		String effectiveContextId = contextId;
		if (response instanceof Task task) {
			effectiveTaskId = effectiveTaskId == null ? task.id() : effectiveTaskId;
			effectiveContextId = effectiveContextId == null ? task.contextId() : effectiveContextId;
		}
		return new GuideCallResult(title, method, endpoint, requestJson,
				curlOverride == null ? curl(method, endpoint, requestJson) : curlOverride, responseType, humanReadable,
				responseJson, stateBefore, stateAfter, effectiveTaskId, effectiveContextId, trace, true, httpStatus,
				null);
	}

	private String curl(String method, String endpoint, String requestJson) {
		String url = this.properties.serverUrl() + endpoint;
		if ("GET".equals(method) || requestJson == null || requestJson.isBlank()) {
			return "curl \"" + url + "\"";
		}
		return "curl -X " + method + " \"" + url + "\" -H \"Content-Type: application/json\" -d '" + requestJson + "'";
	}

	private GuideTraceEvent describeEvent(String stage, ClientEvent event) {
		String description;
		if (event instanceof MessageEvent messageEvent) {
			description = "MessageEvent: " + messageText(messageEvent.getMessage());
		}
		else if (event instanceof TaskEvent taskEvent) {
			description = "TaskEvent: " + taskEvent.getTask().id() + " -> " + taskEvent.getTask().status().state();
		}
		else if (event instanceof TaskUpdateEvent taskUpdateEvent) {
			description = "TaskUpdateEvent: " + taskUpdateEvent.getTask().id() + " -> "
					+ taskUpdateEvent.getTask().status().state();
		}
		else {
			description = event.getClass().getSimpleName();
		}
		String rawJson = null;
		try {
			rawJson = JsonUtil.toJson(event);
		}
		catch (JsonProcessingException ex) {
			rawJson = null;
		}
		return new GuideTraceEvent(Instant.now(), stage, description, rawJson);
	}

	private GuideTraceEvent describeSseEvent(String rawJson) {
		String description = "Subscription event";
		try {
			Object parsed = JsonUtil.fromJson(rawJson, Object.class);
			if (parsed instanceof Map<?, ?> map) {
				Object updateEvent = map.get("updateEvent");
				Object task = map.get("task");
				if (updateEvent instanceof Map<?, ?> updateMap) {
					Object statusUpdate = updateMap.get("statusUpdate");
					if (statusUpdate instanceof Map<?, ?> statusMap) {
						Object taskId = statusMap.get("taskId");
						Object status = statusMap.get("status");
						Object state = status instanceof Map<?, ?> statusDetails ? statusDetails.get("state") : null;
						description = "TaskUpdateEvent: " + valueOf(taskId) + " -> " + valueOf(state);
					}
					else if (updateEvent instanceof Map<?, ?> artifactMap
							&& artifactMap.containsKey("artifactUpdate")) {
						Object artifactUpdate = artifactMap.get("artifactUpdate");
						if (artifactUpdate instanceof Map<?, ?> artifactUpdateMap) {
							Object taskId = artifactUpdateMap.get("taskId");
							description = "TaskUpdateEvent: " + valueOf(taskId) + " -> artifact";
						}
					}
				}
				else if (task instanceof Map<?, ?> taskMap) {
					Object taskId = taskMap.get("id");
					Object status = taskMap.get("status");
					Object state = status instanceof Map<?, ?> statusMap ? statusMap.get("state") : null;
					description = "TaskEvent: " + valueOf(taskId) + " -> " + valueOf(state);
				}
			}
		}
		catch (Exception ex) {
			// Fall back to generic description.
		}
		return new GuideTraceEvent(Instant.now(), "subscribe", description, rawJson);
	}

	private Task extractTask(String rawJson) {
		try {
			Object parsed = JsonUtil.fromJson(rawJson, Object.class);
			if (parsed instanceof Map<?, ?> map) {
				Object task = map.get("task");
				if (task instanceof Map<?, ?> taskMap) {
					return JsonUtil.fromJson(toJson(taskMap), Task.class);
				}
			}
		}
		catch (Exception ex) {
			return null;
		}
		return null;
	}

	private String valueOf(Object value) {
		return value == null ? "n/a" : String.valueOf(value);
	}

	private String messageText(Message message) {
		StringBuilder builder = new StringBuilder();
		if (message.parts() != null) {
			message.parts().forEach(part -> {
				if (part instanceof TextPart textPart) {
					builder.append(textPart.text());
				}
			});
		}
		return builder.toString();
	}

	private String humanReadable(Object response) {
		if (response instanceof Message message) {
			return "Message: " + messageText(message);
		}
		if (response instanceof AgentCard agentCard) {
			return "Discovered agent " + agentCard.name();
		}
		if (response instanceof Task task) {
			return "Task " + task.id() + " is " + task.status().state();
		}
		if (response instanceof TaskPushNotificationConfig config) {
			return "Push config " + config.id() + " for task " + config.taskId();
		}
		if (response instanceof Map<?, ?> map && map.containsKey("taskId") && map.containsKey("url")) {
			Object id = map.get("id");
			Object taskId = map.get("taskId");
			return "Push config " + (id == null ? "created" : id) + " for task " + (taskId == null ? "n/a" : taskId);
		}
		Integer taskCount = readCount(response, "tasks");
		if (taskCount != null) {
			return "Listed " + taskCount + " tasks";
		}
		Integer configCount = readCount(response, "configs");
		if (configCount != null) {
			return "Listed " + configCount + " push configs";
		}
		return response == null ? "No response" : response.getClass().getSimpleName();
	}

	private String responseType(Object response) {
		if (response == null) {
			return "Empty";
		}
		if (response instanceof TaskPushNotificationConfig) {
			return "TaskPushNotificationConfig";
		}
		if (response instanceof Map<?, ?> map && map.containsKey("taskId") && map.containsKey("url")) {
			return "TaskPushNotificationConfig";
		}
		return response.getClass().getSimpleName();
	}

	private Integer readCount(Object response, String accessor) {
		if (response == null) {
			return null;
		}
		Method method = ReflectionUtils.findMethod(response.getClass(), accessor);
		if (method == null) {
			return null;
		}
		Object value = ReflectionUtils.invokeMethod(method, response);
		if (value instanceof List<?> list) {
			return list.size();
		}
		return null;
	}

	private TaskState parseTaskState(String value) {
		String normalized = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
		if (!normalized.startsWith("TASK_STATE_")) {
			normalized = "TASK_STATE_" + normalized;
		}
		return TaskState.valueOf(normalized);
	}

	private String toJson(Object value) {
		try {
			return JsonUtil.toJson(value);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize guide payload", e);
		}
	}

	private String sendPushNotificationConfigRequest(AgentCard agentCard, String uri, String method, String body)
			throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uri))
			.header("A2A-Version", resolveProtocolVersion(agentCard))
			.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		if (body != null) {
			builder.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.method(method, HttpRequest.BodyPublishers.ofString(body));
		}
		else {
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		}
		HttpResponse<String> response = HttpClient.newHttpClient()
			.send(builder.build(), HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IllegalStateException("Push notification request failed with HTTP " + response.statusCode()
					+ (response.body() == null || response.body().isBlank() ? "" : ": " + response.body()));
		}
		return response.body();
	}

	private record ExchangeContext(AgentCard agentCard, boolean streaming) {

		private Client client() throws Exception {
			ClientConfig config = new ClientConfig.Builder().setStreaming(this.streaming).build();
			return Client.builder(this.agentCard)
				.clientConfig(config)
				.withTransport(RestTransport.class, new RestTransportConfigBuilder())
				.build();
		}

	}

	private String resolveProtocolVersion(AgentCard agentCard) {
		if (agentCard.supportedInterfaces() != null && !agentCard.supportedInterfaces().isEmpty()
				&& agentCard.supportedInterfaces().get(0).protocolVersion() != null
				&& !agentCard.supportedInterfaces().get(0).protocolVersion().isBlank()) {
			return agentCard.supportedInterfaces().get(0).protocolVersion();
		}
		return "1.0";
	}

}
