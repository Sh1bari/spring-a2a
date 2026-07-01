package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.example;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.config.SpringBootRestClientExampleProperties;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideCallResult;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideTraceEvent;
import lombok.RequiredArgsConstructor;
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
import org.a2aproject.sdk.spec.Message;
import org.a2aproject.sdk.spec.Task;
import org.a2aproject.sdk.spec.TextPart;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class SdkGuideMessageA2AClient implements GuideMessageA2AClient {

	private final SpringBootRestClientExampleProperties properties;

	@Override
	public GuideCallResult sendMessage(AgentCard agentCard, String prompt) throws Exception {
		ExchangeContext context = new ExchangeContext(agentCard, false);
		Message message = Message.builder().role(Message.Role.ROLE_USER).parts(List.of(new TextPart(prompt))).build();
		String requestJson = toJson(message);
		List<GuideTraceEvent> trace = new ArrayList<>();
		AtomicReference<Object> responseRef = new AtomicReference<>();
		AtomicReference<Throwable> errorRef = new AtomicReference<>();
		try (Client client = context.client()) {
			client.sendMessage(message, List.of((event, card) -> {
				trace.add(describeEvent("client", event));
				if (event instanceof MessageEvent messageEvent) {
					responseRef.set(messageEvent.getMessage());
				}
				else if (event instanceof TaskEvent taskEvent) {
					responseRef.set(taskEvent.getTask());
				}
				else if (event instanceof TaskUpdateEvent taskUpdateEvent) {
					responseRef.set(taskUpdateEvent.getTask());
				}
			}), throwable -> errorRef.set(throwable), null);
		}
		if (errorRef.get() != null) {
			throw new IllegalStateException("A2A sendMessage failed", errorRef.get());
		}
		return toCallResult("Send Message", "POST", "/message:send", requestJson, null, 200, responseRef.get(), trace,
				null, null, null, null);
	}

	@Override
	public GuideCallResult streamMessage(AgentCard agentCard, String prompt, int timeoutSeconds,
			java.util.function.Consumer<GuideTraceEvent> traceConsumer) throws Exception {
		ExchangeContext context = new ExchangeContext(agentCard, true);
		Message message = Message.builder().role(Message.Role.ROLE_USER).parts(List.of(new TextPart(prompt))).build();
		String requestJson = toJson(message);
		List<GuideTraceEvent> trace = new ArrayList<>();
		AtomicReference<Object> responseRef = new AtomicReference<>();
		AtomicReference<Throwable> errorRef = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(1);
		try (Client client = context.client()) {
			client.sendMessage(message, List.of((event, card) -> {
				GuideTraceEvent traceEvent = describeEvent("stream", event);
				trace.add(traceEvent);
				traceConsumer.accept(traceEvent);
				if (event instanceof MessageEvent messageEvent) {
					responseRef.set(messageEvent.getMessage());
					latch.countDown();
				}
				else if (event instanceof TaskEvent taskEvent) {
					responseRef.set(taskEvent.getTask());
					if (taskEvent.getTask().status().state().isFinal()) {
						latch.countDown();
					}
				}
				else if (event instanceof TaskUpdateEvent taskUpdateEvent) {
					responseRef.set(taskUpdateEvent.getTask());
					if (taskUpdateEvent.getTask().status().state().isFinal()) {
						latch.countDown();
					}
				}
			}), throwable -> {
				errorRef.set(throwable);
				latch.countDown();
			}, null);
			if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Timed out waiting for streaming response");
			}
		}
		if (errorRef.get() != null) {
			throw new IllegalStateException("A2A stream failed", errorRef.get());
		}
		return toCallResult("Stream Task Updates", "POST", "/message:stream", requestJson, null, 200, responseRef.get(),
				trace, null, null, null, null);
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
		return new GuideTraceEvent(java.time.Instant.now(), stage, description, rawJson);
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
		if (response == null) {
			return "No response";
		}
		if (response instanceof Task task) {
			return "Task " + task.id() + " is " + task.status().state();
		}
		return response.getClass().getSimpleName();
	}

	private String responseType(Object response) {
		if (response == null) {
			return "Empty";
		}
		return response.getClass().getSimpleName();
	}

	private String toJson(Object value) {
		try {
			return JsonUtil.toJson(value);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to serialize guide payload", e);
		}
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

}
