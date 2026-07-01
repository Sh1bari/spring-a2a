package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.service;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.config.SpringBootRestClientExampleProperties;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.client.GuideA2AClient;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideCallResult;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuidePageModel;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioDefinition;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioOutcome;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioRequest;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideTraceEvent;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.scenario.GuideScenarioCatalog;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.scenario.GuidePlaygroundTemplateCatalog;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session.GuideSessionService;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session.GuideSessionState;
import lombok.RequiredArgsConstructor;
import org.a2aproject.sdk.A2A;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import org.a2aproject.sdk.spec.AgentCard;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class GuideService {

	private final SpringBootRestClientExampleProperties properties;

	private final GuideA2AClient guideA2AClient;

	private final GuideScenarioCatalog catalog;

	private final GuidePlaygroundTemplateCatalog playgroundTemplates;

	private final GuideSessionService sessionService;

	private final Map<String, ScenarioRunner> runners = createRunners();

	public GuidePageModel loadPage(HttpSession session) {
		GuideSessionState state = this.sessionService.getOrCreate(session);
		boolean serverAvailable = false;
		String serverStatusMessage;
		try {
			GuideCallResult discovery = this.guideA2AClient.discoverAgent();
			AgentCard agentCard = JsonUtil.fromJson(discovery.responseJson(), AgentCard.class);
			state.setAgentCardJson(discovery.responseJson());
			state.setAgentCardName(agentCard.name());
			serverAvailable = true;
			serverStatusMessage = "Server reachable";
		}
		catch (Exception ex) {
			serverStatusMessage = "The A2A server is unavailable. Start the server or verify the configured server URL.";
		}

		return new GuidePageModel(this.properties.serverUrl(), serverAvailable, serverStatusMessage, "Client: running",
				"REST", resolveA2aSdkVersion(), SpringBootVersion.getVersion(), this.playgroundTemplates.templates(),
				state.getCurrentTaskId(), state.getCurrentContextId(), this.properties.callbackUrl());
	}

	public GuideScenarioOutcome runScenario(String scenarioId, GuideScenarioRequest request, HttpSession session) {
		return runScenario(scenarioId, request, session, event -> {
		});
	}

	public GuideScenarioOutcome runScenario(String scenarioId, GuideScenarioRequest request, HttpSession session,
			Consumer<GuideTraceEvent> liveTraceConsumer) {
		GuideSessionState state = this.sessionService.getOrCreate(session);
		GuideScenarioDefinition definition;
		try {
			definition = this.catalog.findScenario(scenarioId);
		}
		catch (IllegalArgumentException ex) {
			GuideScenarioOutcome failure = new GuideScenarioOutcome(scenarioId, scenarioId, false,
					this.properties.serverUrl(), List.of(), "Scenario not available",
					List.of("The requested scenario is not available on this page."),
					List.of("Refresh the page so the client can load the current scenario list."), null, null,
					List.of(),
					new io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideErrorView(
							"Unknown scenario", ex.getMessage(), null, null, "Refresh the page and try again."));
			state.record(failure);
			return failure;
		}
		ScenarioRunner runner = this.runners.get(scenarioId);
		if (runner == null) {
			GuideScenarioOutcome failure = new GuideScenarioOutcome(scenarioId, scenarioId, false,
					this.properties.serverUrl(), List.of(), "Scenario not available",
					List.of("The requested scenario is not available on this page."),
					List.of("Refresh the page so the client can load the current scenario list."), null, null,
					List.of(),
					new io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideErrorView(
							"Unknown scenario", "Unknown scenario: " + scenarioId, null, null,
							"Refresh the page and try again."));
			state.record(failure);
			return failure;
		}
		try {
			AgentCard agentCard = resolveAgentCard(state);
			GuideScenarioOutcome outcome = runner.run(definition, request, state, agentCard, liveTraceConsumer);
			state.record(outcome);
			return outcome;
		}
		catch (Exception ex) {
			GuideScenarioOutcome failure = failure(scenarioId, ex);
			state.record(failure);
			return failure;
		}
	}

	public GuidePageModel reset(HttpSession session) {
		this.sessionService.reset(session);
		return loadPage(session);
	}

	public AgentCard resolveAgentCard(GuideSessionState state) throws Exception {
		if (state.getAgentCardJson() != null && !state.getAgentCardJson().isBlank()) {
			return JsonUtil.fromJson(state.getAgentCardJson(), AgentCard.class);
		}
		GuideCallResult discovery = this.guideA2AClient.discoverAgent();
		AgentCard agentCard = JsonUtil.fromJson(discovery.responseJson(), AgentCard.class);
		state.setAgentCardJson(discovery.responseJson());
		state.setAgentCardName(agentCard.name());
		return agentCard;
	}

	private GuideScenarioOutcome runDiscover(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		GuideCallResult call = this.guideA2AClient.discoverAgent();
		AgentCard discovered = JsonUtil.fromJson(call.responseJson(), AgentCard.class);
		state.setAgentCardJson(call.responseJson());
		state.setAgentCardName(discovered.name());
		List<String> whatHappened = List.of("The client called the discovery endpoint.",
				"The server returned the Agent Card.", "The client cached the card for later steps.");
		return outcome(definition, state, List.of(call), whatHappened,
				List.of("Discovery tells the client what the server can do.",
						"The Agent Card controls later protocol choices.", "The client does not guess capabilities."),
				"Agent Card discovered");
	}

	private GuideScenarioOutcome runSendMessage(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		String prompt = resolvePrompt(request, definition);
		GuideCallResult call = this.guideA2AClient.sendMessage(agentCard, prompt);
		String stateBefore = "No task yet";
		String stateAfter = call.taskId() == null ? "Direct message response" : "Task " + call.taskId() + " created";
		GuideCallResult normalizedCall = normalize(call, stateBefore, stateAfter);
		if (call.taskId() != null) {
			state.setCurrentTaskId(call.taskId());
			state.setCurrentContextId(call.contextId());
		}
		return outcome(definition, state, List.of(normalizedCall),
				List.of("The browser submitted the guide form to the client.",
						"The client created a real A2A message and sent POST /message:send.",
						"The server responded through the A2A runtime."),
				List.of("Message send is the basic protocol operation.",
						"The response can be a direct Message or a Task depending on agent behavior.",
						"The client can reuse the returned task later."),
				"Message sent");
	}

	private GuideScenarioOutcome runCreateAndInspectTask(GuideScenarioDefinition definition,
			GuideScenarioRequest request, GuideSessionState state, AgentCard agentCard,
			Consumer<GuideTraceEvent> liveTraceConsumer) throws Exception {
		String prompt = resolvePrompt(request, definition);
		GuideCallResult create = normalize(this.guideA2AClient.sendMessage(agentCard, prompt), "No task yet",
				"Task created");
		if (create.taskId() == null) {
			throw new IllegalStateException("Task scenario did not return a task id");
		}
		GuideCallResult inspect = normalize(this.guideA2AClient.inspectTask(agentCard, create.taskId(), null),
				"Task created", "Task fetched");
		state.setCurrentTaskId(inspect.taskId());
		state.setCurrentContextId(inspect.contextId());
		return outcome(definition, state, List.of(create, inspect),
				List.of("The client sent a command that creates a task.", "The server returned a task id.",
						"The client fetched the same task using GET /tasks/{taskId}."),
				List.of("Tasks are addressable protocol objects.",
						"Task inspection is separate from the initial message call.",
						"History and artifacts become visible after the task exists."),
				"Task created and inspected");
	}

	private GuideScenarioOutcome runInspectTask(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		String taskId = resolveTaskId(request, state);
		Integer historyLength = request == null || request.historyLength() == null ? 2 : request.historyLength();
		GuideCallResult inspect = normalize(this.guideA2AClient.inspectTask(agentCard, taskId, null), "Task available",
				"Task inspected");
		state.setCurrentTaskId(inspect.taskId());
		state.setCurrentContextId(inspect.contextId());
		return outcome(definition, state, List.of(inspect),
				List.of("The client called GET /tasks/{taskId}.", "The server returned the current task state.",
						"History length controls how much previous activity is returned."),
				List.of("Task lookup is separate from task creation.",
						"The task remains addressable after it is created.",
						"History and artifacts can be inspected without resending the message."),
				"Task inspected");
	}

	private GuideScenarioOutcome runListTasks(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		String contextId = request != null && request.contextId() != null && !request.contextId().isBlank()
				? request.contextId() : state.getCurrentContextId();
		GuideCallResult call = normalize(this.guideA2AClient.listTasks(agentCard, contextId,
				request == null ? null : request.status(), request == null ? null : request.pageSize(),
				request == null ? null : request.pageToken(), request == null ? null : request.historyLength(),
				request == null ? null : request.includeArtifacts()), "No change", "Tasks listed");
		return outcome(definition, state, List.of(call),
				List.of("The client called GET /tasks.", "The server returned the current task collection.",
						"Filters control what tasks and how much history are included."),
				List.of("List tasks is useful for browsing task state without knowing one id.",
						"Query parameters let you filter by context, status, page size, and history.",
						"This endpoint complements get task and subscribe."),
				"Tasks listed");
	}

	private GuideScenarioOutcome runStreamTask(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		String prompt = resolvePrompt(request, definition);
		List<GuideTraceEvent> liveTrace = new ArrayList<>();
		GuideCallResult stream = normalize(this.guideA2AClient.streamMessage(agentCard, prompt,
				request.streamingTimeoutSeconds() == null ? 20 : request.streamingTimeoutSeconds(), event -> {
					liveTrace.add(event);
					liveTraceConsumer.accept(event);
				}), "Submitted", "Streaming completed");
		GuideCallResult inspect = null;
		if (stream.taskId() != null) {
			inspect = normalize(this.guideA2AClient.inspectTask(agentCard, stream.taskId(), null),
					"Streaming completed", "Task fetched after stream");
			state.setCurrentTaskId(inspect.taskId());
			state.setCurrentContextId(inspect.contextId());
		}
		List<GuideCallResult> steps = inspect == null ? List.of(stream) : List.of(stream, inspect);
		return outcome(definition, state, steps,
				List.of("The client opened a streaming request.", "Events arrived while the request was still open.",
						"The client can fetch the task again after the stream completes."),
				List.of("Streaming is useful for long-running work.",
						"Task updates arrive as events, not only as a final body.",
						"The final task can still be retrieved afterwards."),
				"Streaming finished");
	}

	private GuideScenarioOutcome runSubscribeTask(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		String taskId = resolveTaskIdOrCreate(request, state, agentCard, "/task");
		List<GuideCallResult> subscribeResults = this.guideA2AClient.subscribeToTask(agentCard, taskId);
		GuideCallResult call = subscribeResults.isEmpty()
				? new GuideCallResult("Subscribe to Task", "POST", "/tasks/" + taskId + ":subscribe", null, null,
						"Task", "Subscription completed", null, "Subscribed", "Subscribed", taskId,
						state.getCurrentContextId(), List.of(), true, 200, null)
				: normalize(subscribeResults.get(0), "Subscribed", "Subscribed");
		state.setCurrentTaskId(taskId);
		return outcome(definition, state, List.of(call),
				List.of("The client called POST /tasks/{taskId}:subscribe.",
						"The server attached a streaming subscription to the task.",
						"The client can now receive future updates for that task."),
				List.of("Subscribe is different from streaming a new message.",
						"It uses an existing task id instead of creating one.",
						"It is useful when another process will keep working on the same task."),
				"Task subscription attached");
	}

	private GuideScenarioOutcome runCancelTask(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		String taskId = request != null && request.taskId() != null && !request.taskId().isBlank() ? request.taskId()
				: state.getCurrentTaskId();
		GuideCallResult create = null;
		if (taskId == null || taskId.isBlank()) {
			create = normalize(this.guideA2AClient.sendMessage(agentCard, resolvePrompt(request, definition)),
					"No task", "Cancelable task created");
			taskId = create.taskId();
			if (taskId == null) {
				throw new IllegalStateException("Cancelable task did not return a task id");
			}
		}
		GuideCallResult cancel = normalize(this.guideA2AClient.cancelTask(agentCard, taskId), "Working", "Canceled");
		GuideCallResult inspect = normalize(this.guideA2AClient.inspectTask(agentCard, taskId, null), "Canceled",
				"Task fetched after cancel");
		state.setCurrentTaskId(inspect.taskId());
		state.setCurrentContextId(inspect.contextId());
		List<GuideCallResult> steps = create == null ? List.of(cancel, inspect) : List.of(create, cancel, inspect);
		return outcome(definition, state, steps,
				List.of(create == null ? "The client used the current task id or the id from the request."
						: "The client created a long-running task.", "The client called the cancel endpoint.",
						"The client fetched the task again to verify the final state."),
				List.of("Cancellation is a protocol operation.",
						"Cancel changes task state but does not delete the task.",
						"The same task can be inspected after cancellation."),
				"Task canceled");
	}

	private GuideScenarioOutcome runCreatePushNotificationConfig(GuideScenarioDefinition definition,
			GuideScenarioRequest request, GuideSessionState state, AgentCard agentCard,
			Consumer<GuideTraceEvent> liveTraceConsumer) throws Exception {
		String taskId = resolveTaskIdOrCreate(request, state, agentCard, "/task");
		String callbackUrl = request != null && request.callbackUrl() != null && !request.callbackUrl().isBlank()
				? request.callbackUrl() : this.properties.callbackUrl();
		GuideCallResult call = normalize(
				this.guideA2AClient.createTaskPushNotificationConfig(agentCard, taskId, callbackUrl), "No config yet",
				"Push config created");
		state.setCurrentTaskId(taskId);
		if (call.responseJson() != null) {
			state.setCurrentPushNotificationConfigId(extractConfigId(call.responseJson()));
		}
		return outcome(definition, state, List.of(call),
				List.of("The client called POST /tasks/{taskId}/pushNotificationConfigs.",
						"The server stored a callback URL for the task.",
						"The callback URL lets the server push updates back to the client."),
				List.of("Push notification configs tell the server where to call back.",
						"They are separate from task subscription and streaming.",
						"The config can be fetched, listed, and deleted."),
				"Push config created");
	}

	private GuideScenarioOutcome runGetPushNotificationConfig(GuideScenarioDefinition definition,
			GuideScenarioRequest request, GuideSessionState state, AgentCard agentCard,
			Consumer<GuideTraceEvent> liveTraceConsumer) throws Exception {
		String taskId = resolveTaskIdOrCreate(request, state, agentCard, "/task");
		String configId = resolveConfigIdOrCreate(request, state, agentCard, taskId);
		GuideCallResult call = normalize(this.guideA2AClient.getTaskPushNotificationConfig(agentCard, taskId, configId),
				"Stored", "Push config fetched");
		state.setCurrentTaskId(taskId);
		state.setCurrentPushNotificationConfigId(configId);
		return outcome(definition, state, List.of(call),
				List.of("The client called GET /tasks/{taskId}/pushNotificationConfigs/{configId}.",
						"The server returned one stored callback config.",
						"This is useful when you already know the config id."),
				List.of("Get push config reads a single stored callback configuration.",
						"It is separate from listing all configs.", "It requires both task id and config id."),
				"Push config fetched");
	}

	private GuideScenarioOutcome runListPushNotificationConfigs(GuideScenarioDefinition definition,
			GuideScenarioRequest request, GuideSessionState state, AgentCard agentCard,
			Consumer<GuideTraceEvent> liveTraceConsumer) throws Exception {
		String taskId = resolveTaskIdOrCreate(request, state, agentCard, "/task");
		GuideCallResult call = normalize(
				this.guideA2AClient.listTaskPushNotificationConfigs(agentCard, taskId,
						request == null ? null : request.pageSize(), request == null ? null : request.pageToken()),
				"Stored", "Push configs listed");
		state.setCurrentTaskId(taskId);
		return outcome(definition, state, List.of(call),
				List.of("The client called GET /tasks/{taskId}/pushNotificationConfigs.",
						"The server returned the configs registered for that task.",
						"Page size and page token control the list window."),
				List.of("List push configs is the overview endpoint for callbacks.",
						"It is useful when a task has multiple delivery destinations.", "The response can be paged."),
				"Push configs listed");
	}

	private GuideScenarioOutcome runDeletePushNotificationConfig(GuideScenarioDefinition definition,
			GuideScenarioRequest request, GuideSessionState state, AgentCard agentCard,
			Consumer<GuideTraceEvent> liveTraceConsumer) throws Exception {
		String taskId = resolveTaskIdOrCreate(request, state, agentCard, "/task");
		String configId = resolveConfigIdOrCreate(request, state, agentCard, taskId);
		GuideCallResult call = normalize(
				this.guideA2AClient.deleteTaskPushNotificationConfig(agentCard, taskId, configId), "Stored",
				"Push config deleted");
		return outcome(definition, state, List.of(call),
				List.of("The client called DELETE /tasks/{taskId}/pushNotificationConfigs/{configId}.",
						"The server removed the callback config.", "Deletion does not affect the task itself."),
				List.of("Delete push config is how you stop callbacks for one destination.",
						"It only removes the config, not the task.",
						"After deletion you can list configs again to confirm it is gone."),
				"Push config deleted");
	}

	private GuideScenarioOutcome runCallbackDemo(GuideScenarioDefinition definition, GuideScenarioRequest request,
			GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
			throws Exception {
		String prompt = resolvePrompt(request, definition);
		AtomicReference<String> createdTaskId = new AtomicReference<>();
		AtomicReference<String> createdContextId = new AtomicReference<>();
		AtomicReference<String> createdConfigId = new AtomicReference<>();
		List<GuideTraceEvent> liveTrace = new ArrayList<>();
		GuideCallResult call = this.guideA2AClient.streamMessage(agentCard, prompt,
				request == null || request.streamingTimeoutSeconds() == null ? 25 : request.streamingTimeoutSeconds(),
				traceEvent -> {
					liveTrace.add(traceEvent);
					liveTraceConsumer.accept(traceEvent);
					if (createdTaskId.get() != null || traceEvent.rawJson() == null || traceEvent.rawJson().isBlank()) {
						return;
					}
					String taskId = extractTaskId(traceEvent.rawJson());
					if (taskId == null || taskId.isBlank()) {
						return;
					}
					createdTaskId.compareAndSet(null, taskId);
					String contextId = extractContextId(traceEvent.rawJson());
					if (contextId != null && !contextId.isBlank()) {
						createdContextId.compareAndSet(null, contextId);
					}
					state.setCurrentTaskId(taskId);
					if (contextId != null && !contextId.isBlank()) {
						state.setCurrentContextId(contextId);
					}
					if (createdConfigId.get() == null) {
						try {
							GuideCallResult config = this.guideA2AClient.createTaskPushNotificationConfig(agentCard,
									taskId, this.properties.callbackUrl());
							String configId = extractConfigId(config.responseJson());
							if (configId != null && !configId.isBlank()) {
								createdConfigId.compareAndSet(null, configId);
								state.setCurrentPushNotificationConfigId(configId);
							}
						}
						catch (Exception ex) {
							// The demo keeps working even if the callback config cannot
							// be created.
						}
					}
				});
		String stateAfter = call.taskId() == null ? "Streaming completed" : "Callback demo task completed";
		GuideCallResult normalizedCall = new GuideCallResult(call.title(), call.method(), call.endpoint(),
				call.requestJson(), call.curl(), call.responseType(), call.humanReadableResponse(), call.responseJson(),
				"Submitted", stateAfter, call.taskId(), call.contextId(), liveTrace, call.success(), call.httpStatus(),
				call.error());
		if (call.taskId() != null) {
			state.setCurrentTaskId(call.taskId());
			state.setCurrentContextId(call.contextId());
		}
		return outcome(definition, state, List.of(normalizedCall),
				List.of("The client sent a long-running task to the server.",
						"The server emitted progress events once per second.",
						"Push callbacks can arrive while the task is still running."),
				List.of("This demo is designed to test callback delivery.",
						"The client registers a callback as soon as the task id appears.",
						"The client inbox should show each callback POST as it arrives."),
				"Callback demo started");
	}

	private GuideScenarioOutcome outcome(GuideScenarioDefinition definition, GuideSessionState state,
			List<GuideCallResult> steps, List<String> whatHappened, List<String> learningOutcome, String summary) {
		String currentTaskId = steps.stream()
			.map(GuideCallResult::taskId)
			.filter(Objects::nonNull)
			.reduce((first, second) -> second)
			.orElse(state.getCurrentTaskId());
		String currentContextId = steps.stream()
			.map(GuideCallResult::contextId)
			.filter(Objects::nonNull)
			.reduce((first, second) -> second)
			.orElse(state.getCurrentContextId());
		return new GuideScenarioOutcome(definition.id(), definition.title(), true, this.properties.serverUrl(), steps,
				summary, whatHappened, learningOutcome, currentTaskId, currentContextId, collectTrace(steps), null);
	}

	private GuideScenarioOutcome failure(GuideScenarioDefinition definition, Exception ex) {
		return new GuideScenarioOutcome(definition.id(), definition.title(), false, this.properties.serverUrl(),
				List.of(), "Scenario failed", List.of("The client attempted the scenario and received an error."),
				List.of("The error is shown in the guide so the user can fix the server or request."), null, null,
				List.of(), new io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideErrorView(
						"Scenario failed", ex.getMessage(), null, null, "Verify the server is running and try again."));
	}

	private GuideScenarioOutcome failure(String scenarioId, Exception ex) {
		return new GuideScenarioOutcome(scenarioId, scenarioId, false, this.properties.serverUrl(), List.of(),
				"Scenario failed", List.of("The client attempted the scenario and received an error."),
				List.of("The error is shown in the guide so the user can fix the server or request."), null, null,
				List.of(),
				new io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideErrorView(
						"Scenario failed", ex.getMessage(), null, null,
						"Refresh the page and try the scenario again."));
	}

	private GuideCallResult normalize(GuideCallResult call, String stateBefore, String stateAfter) {
		return new GuideCallResult(call.title(), call.method(), call.endpoint(), call.requestJson(), call.curl(),
				call.responseType(), call.humanReadableResponse(), call.responseJson(), stateBefore, stateAfter,
				call.taskId(), call.contextId(), call.traceEvents(), call.success(), call.httpStatus(), call.error());
	}

	private List<GuideTraceEvent> collectTrace(List<GuideCallResult> steps) {
		List<GuideTraceEvent> traceEvents = new ArrayList<>();
		for (GuideCallResult step : steps) {
			traceEvents.addAll(step.traceEvents());
		}
		return traceEvents;
	}

	private String resolvePrompt(GuideScenarioRequest request, GuideScenarioDefinition definition) {
		if (request != null && request.prompt() != null && !request.prompt().isBlank()) {
			return request.prompt();
		}
		return definition.defaultPrompt();
	}

	private String resolveTaskId(GuideScenarioRequest request, GuideSessionState state) {
		if (request != null && request.taskId() != null && !request.taskId().isBlank()) {
			return request.taskId();
		}
		if (state.getCurrentTaskId() != null && !state.getCurrentTaskId().isBlank()) {
			return state.getCurrentTaskId();
		}
		throw new IllegalStateException("No task id is available. Run a task scenario first or enter a task id.");
	}

	private String resolveTaskIdOrCreate(GuideScenarioRequest request, GuideSessionState state, AgentCard agentCard,
			String prompt) throws Exception {
		if (request != null && request.taskId() != null && !request.taskId().isBlank()) {
			return request.taskId();
		}
		if (state.getCurrentTaskId() != null && !state.getCurrentTaskId().isBlank()) {
			return state.getCurrentTaskId();
		}
		GuideCallResult create = normalize(this.guideA2AClient.sendMessage(agentCard, prompt), "No task yet",
				"Task created");
		if (create.taskId() == null) {
			throw new IllegalStateException("Could not create a task for this scenario");
		}
		state.setCurrentTaskId(create.taskId());
		state.setCurrentContextId(create.contextId());
		return create.taskId();
	}

	private String resolveConfigId(GuideScenarioRequest request, GuideSessionState state) {
		if (request != null && request.configId() != null && !request.configId().isBlank()) {
			return request.configId();
		}
		if (state.getCurrentPushNotificationConfigId() != null
				&& !state.getCurrentPushNotificationConfigId().isBlank()) {
			return state.getCurrentPushNotificationConfigId();
		}
		throw new IllegalStateException("No push notification config id is available. Create one first or enter one.");
	}

	private String resolveConfigIdOrCreate(GuideScenarioRequest request, GuideSessionState state, AgentCard agentCard,
			String taskId) throws Exception {
		if (request != null && request.configId() != null && !request.configId().isBlank()) {
			return request.configId();
		}
		if (state.getCurrentPushNotificationConfigId() != null
				&& !state.getCurrentPushNotificationConfigId().isBlank()) {
			return state.getCurrentPushNotificationConfigId();
		}
		GuideCallResult create = normalize(
				this.guideA2AClient.createTaskPushNotificationConfig(agentCard, taskId, this.properties.callbackUrl()),
				"No config yet", "Push config created");
		String configId = extractConfigId(create.responseJson());
		if (configId == null || configId.isBlank()) {
			throw new IllegalStateException("Could not create a push notification config for this scenario");
		}
		state.setCurrentPushNotificationConfigId(configId);
		return configId;
	}

	private String extractConfigId(String responseJson) {
		try {
			Object parsed = JsonUtil.fromJson(responseJson, Object.class);
			if (parsed instanceof Map<?, ?> map) {
				Object value = map.get("id");
				if (value == null) {
					value = map.get("configId");
				}
				return value == null ? null : String.valueOf(value);
			}
		}
		catch (Exception ex) {
			return null;
		}
		return null;
	}

	private String extractTaskId(String rawJson) {
		try {
			Object parsed = JsonUtil.fromJson(rawJson, Object.class);
			return findNestedString(parsed, "id");
		}
		catch (Exception ex) {
			return null;
		}
	}

	private String extractContextId(String rawJson) {
		try {
			Object parsed = JsonUtil.fromJson(rawJson, Object.class);
			return findNestedString(parsed, "contextId");
		}
		catch (Exception ex) {
			return null;
		}
	}

	private String findNestedString(Object value, String key) {
		if (value instanceof Map<?, ?> map) {
			Object direct = map.get(key);
			if (direct != null) {
				return String.valueOf(direct);
			}
			for (Object child : map.values()) {
				String nested = findNestedString(child, key);
				if (nested != null) {
					return nested;
				}
			}
		}
		else if (value instanceof List<?> list) {
			for (Object child : list) {
				String nested = findNestedString(child, key);
				if (nested != null) {
					return nested;
				}
			}
		}
		return null;
	}

	private String resolveA2aSdkVersion() {
		String version = A2A.class.getPackage().getImplementationVersion();
		return version == null || version.isBlank() ? "1.1.0.Final" : version;
	}

	private Map<String, ScenarioRunner> createRunners() {
		Map<String, ScenarioRunner> runners = new LinkedHashMap<>();
		runners.put("discover-agent", this::runDiscover);
		runners.put("send-message", this::runSendMessage);
		runners.put("create-and-inspect-task", this::runCreateAndInspectTask);
		runners.put("inspect-task", this::runInspectTask);
		runners.put("list-tasks", this::runListTasks);
		runners.put("stream-task-updates", this::runStreamTask);
		runners.put("subscribe-task", this::runSubscribeTask);
		runners.put("cancel-task", this::runCancelTask);
		runners.put("create-push-config", this::runCreatePushNotificationConfig);
		runners.put("get-push-config", this::runGetPushNotificationConfig);
		runners.put("list-push-configs", this::runListPushNotificationConfigs);
		runners.put("delete-push-config", this::runDeletePushNotificationConfig);
		runners.put("callback-demo", this::runCallbackDemo);
		return runners;
	}

	@FunctionalInterface
	private interface ScenarioRunner {

		GuideScenarioOutcome run(GuideScenarioDefinition definition, GuideScenarioRequest request,
				GuideSessionState state, AgentCard agentCard, Consumer<GuideTraceEvent> liveTraceConsumer)
				throws Exception;

	}

}
