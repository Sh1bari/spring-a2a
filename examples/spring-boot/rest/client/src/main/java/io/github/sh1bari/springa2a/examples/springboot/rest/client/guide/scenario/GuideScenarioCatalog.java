package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.scenario;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideReferenceEntry;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GuideScenarioCatalog {

	private final List<GuideScenarioDefinition> scenarios = List.of(new GuideScenarioDefinition("discover-agent", 1,
			"Discover Agent", "Learn what the server supports",
			"Discovery is the first step in any A2A flow. The client reads the Agent Card to understand capabilities and transport details.",
			List.of("The agent card tells the client which transport is available",
					"Capabilities influence which flows can run safely", "Discovery is a real HTTP request"),
			"GET", "/.well-known/agent-card.json", List.of("AgentCard", "capabilities", "transport", "discovery"), null,
			false, false, false),
			new GuideScenarioDefinition("send-message", 2, "Send Message",
					"Send a normal prompt and see a direct A2A response",
					"Message send is the simplest A2A operation. The client posts a user message and the server returns either a Message or a Task depending on the agent behavior.",
					List.of("The request uses a real A2A Message",
							"The server may respond synchronously or with a task", "Prompt text is editable in the UI"),
					"POST", "/message:send", List.of("Message", "AgentExecutor", "response"), "hello from the guide",
					false, false, false),
			new GuideScenarioDefinition("create-and-inspect-task", 3, "Create and Inspect Task",
					"Create a predictable task and inspect it afterwards",
					"Some agent work is asynchronous. This scenario shows how a task is created and then fetched by task ID.",
					List.of("Tasks have IDs and context IDs", "The same task can be fetched later",
							"Task state can be inspected independently of the initial request"),
					"POST", "/message:send", List.of("Task", "TaskQueryParams", "history"), "/task", false, true,
					false),
			new GuideScenarioDefinition("inspect-task", 4, "Inspect Task",
					"Look up an existing task by id and inspect the current state",
					"Task inspection lets the client read the latest state, history, and artifacts without sending a new message.",
					List.of("The task id is supplied by the previous step or pasted in the form",
							"The server returns the current task state",
							"History length controls the amount of detail"),
					"GET", "/tasks/{taskId}", List.of("Task", "TaskQueryParams", "history"), null, false, true, false),
			new GuideScenarioDefinition("list-tasks", 5, "List Tasks",
					"Fetch the current task collection and optionally filter it",
					"List tasks shows what the server currently knows about without needing a single task id.",
					List.of("Filters are sent as query parameters", "Useful for browsing a task collection",
							"History and artifacts can be included"),
					"GET", "/tasks", List.of("ListTasksParams", "TaskState", "pagination"), null, false, false, false),
			new GuideScenarioDefinition("stream-task-updates", 6, "Stream Task Updates",
					"Watch a task move through streaming events",
					"Streaming is how the client receives task progress without polling. The guide shows the live event sequence as the server produces it.",
					List.of("Streaming exposes live task progress", "Events arrive in order",
							"The UI can show the task lifecycle as it happens"),
					"POST", "/message:stream", List.of("TaskEvent", "TaskUpdateEvent", "artifact"), "stream this", true,
					true, false),
			new GuideScenarioDefinition("subscribe-task", 7, "Subscribe to Task",
					"Subscribe to an existing task and wait for future updates",
					"Subscribe is different from streaming a new message. It watches a task that already exists and is useful when another process continues the work.",
					List.of("Requires an existing task id", "Returns a live server-side subscription",
							"Useful when progress happens elsewhere"),
					"POST", "/tasks/{taskId}:subscribe", List.of("TaskIdParams", "TaskEvent", "TaskUpdateEvent"), null,
					true, true, false),
			new GuideScenarioDefinition("cancel-task", 8, "Cancel Task", "Create a long-running task and cancel it",
					"Cancellation is a separate protocol operation. The guide shows that a task can be stopped and then fetched again to verify its final state.",
					List.of("Cancel is a protocol operation, not a UI-only action",
							"Task state should change after cancellation", "The task remains available after cancel"),
					"POST", "/tasks/{taskId}:cancel", List.of("CancelTaskParams", "TaskState", "request ID"),
					"/cancel-demo", false, true, true),
			new GuideScenarioDefinition("create-push-config", 9, "Create Push Notification Config",
					"Register a callback URL for an existing task",
					"Push notification configs let the server call the client back when task updates happen asynchronously.",
					List.of("The config belongs to one task", "The callback URL is stored on the server",
							"The config can be inspected or deleted later"),
					"POST", "/tasks/{taskId}/pushNotificationConfigs",
					List.of("TaskPushNotificationConfig", "AuthenticationInfo", "callback"), null, false, true, false),
			new GuideScenarioDefinition("get-push-config", 10, "Get Push Notification Config",
					"Fetch one saved callback config by id",
					"This scenario shows how a stored push configuration is read back from the server.",
					List.of("Requires task id and config id", "Reads one stored callback config",
							"Useful for confirming what was saved"),
					"GET", "/tasks/{taskId}/pushNotificationConfigs/{configId}",
					List.of("GetTaskPushNotificationConfigParams", "TaskPushNotificationConfig"), null, false, true,
					false),
			new GuideScenarioDefinition("list-push-configs", 11, "List Push Notification Configs",
					"List the callback configs associated with a task",
					"This scenario shows the collection view for push notification configs.",
					List.of("Useful when several callbacks are registered", "Supports paging",
							"Helps inspect the current callback setup"),
					"GET", "/tasks/{taskId}/pushNotificationConfigs",
					List.of("ListTaskPushNotificationConfigsParams", "ListTaskPushNotificationConfigsResult"), null,
					false, true, false),
			new GuideScenarioDefinition("delete-push-config", 12, "Delete Push Notification Config",
					"Remove a callback config from a task",
					"Deleting a push config stops callbacks for that destination without deleting the task itself.",
					List.of("Deletion targets one task config", "The task remains available after deletion",
							"Useful for cleanup and demo reset"),
					"DELETE", "/tasks/{taskId}/pushNotificationConfigs/{configId}",
					List.of("DeleteTaskPushNotificationConfigParams", "TaskPushNotificationConfig"), null, false, true,
					false),
			new GuideScenarioDefinition("callback-demo", 13, "Push Callback Demo",
					"Run a 20 second task that emits one event every second",
					"This demo exists to verify that push notifications reach the client inbox while the task is still running.",
					List.of("Create a push config first", "Watch the task emit 20 live updates",
							"Observe incoming callbacks in the client inbox"),
					"POST", "/message:stream", List.of("TaskPushNotificationConfig", "TaskUpdateEvent", "callback"),
					"/callback-demo", true, true, false));

	private final List<GuideReferenceEntry> referenceEntries = List.of(
			new GuideReferenceEntry("GET", "/.well-known/agent-card.json", "Discover the agent", "none", "AgentCard",
					"discover-agent", List.of("AgentCard", "discovery"),
					"curl http://localhost:18080/.well-known/agent-card.json"),
			new GuideReferenceEntry("POST", "/message:send", "Send a user message", "MessageSendParams",
					"Message or Task", "send-message", List.of("Message", "AgentExecutor"),
					"curl -X POST http://localhost:18080/message:send -H 'Content-Type: application/json' -d '{\"message\":{\"role\":\"user\"}}'"),
			new GuideReferenceEntry("GET", "/tasks/{taskId}", "Inspect a task", "TaskQueryParams", "Task",
					"inspect-task", List.of("Task", "TaskQueryParams"),
					"curl 'http://localhost:18080/tasks/task-123?historyLength=2'"),
			new GuideReferenceEntry("GET", "/tasks", "List tasks", "ListTasksParams", "ListTasksResult", "list-tasks",
					List.of("ListTasksParams", "Task", "TaskState"),
					"curl 'http://localhost:18080/tasks?contextId=abc&pageSize=10'"),
			new GuideReferenceEntry("POST", "/message:stream", "Stream task updates", "MessageSendParams",
					"text/event-stream", "stream-task-updates", List.of("TaskEvent", "TaskUpdateEvent"),
					"curl -N -X POST http://localhost:18080/message:stream -H 'Content-Type: application/json' -d '{\"message\":{\"role\":\"user\"}}'"),
			new GuideReferenceEntry("POST", "/tasks/{taskId}:subscribe", "Subscribe to a task", "TaskIdParams",
					"text/event-stream", "subscribe-task", List.of("TaskIdParams", "TaskEvent", "TaskUpdateEvent"),
					"curl -N -X POST http://localhost:18080/tasks/task-123:subscribe"),
			new GuideReferenceEntry("POST", "/tasks/{taskId}/pushNotificationConfigs", "Create push config",
					"TaskPushNotificationConfig", "TaskPushNotificationConfig", "create-push-config",
					List.of("TaskPushNotificationConfig", "AuthenticationInfo"),
					"curl -X POST http://localhost:18080/tasks/task-123/pushNotificationConfigs -H 'Content-Type: application/json' -d '{\"taskId\":\"task-123\"}'"),
			new GuideReferenceEntry("GET", "/tasks/{taskId}/pushNotificationConfigs/{configId}", "Get push config",
					"GetTaskPushNotificationConfigParams", "TaskPushNotificationConfig", "get-push-config",
					List.of("GetTaskPushNotificationConfigParams"),
					"curl http://localhost:18080/tasks/task-123/pushNotificationConfigs/config-1"),
			new GuideReferenceEntry("GET", "/tasks/{taskId}/pushNotificationConfigs", "List push configs",
					"ListTaskPushNotificationConfigsParams", "ListTaskPushNotificationConfigsResult",
					"list-push-configs", List.of("ListTaskPushNotificationConfigsParams"),
					"curl http://localhost:18080/tasks/task-123/pushNotificationConfigs"),
			new GuideReferenceEntry("DELETE", "/tasks/{taskId}/pushNotificationConfigs/{configId}",
					"Delete push config", "DeleteTaskPushNotificationConfigParams", "TaskPushNotificationConfig",
					"delete-push-config", List.of("DeleteTaskPushNotificationConfigParams"),
					"curl -X DELETE http://localhost:18080/tasks/task-123/pushNotificationConfigs/config-1"),
			new GuideReferenceEntry("POST", "/tasks/{taskId}:cancel", "Cancel a task", "CancelTaskParams", "Task",
					"cancel-task", List.of("CancelTaskParams", "TaskState"),
					"curl -X POST http://localhost:18080/tasks/task-123:cancel"));

	public List<GuideScenarioDefinition> scenarios() {
		return this.scenarios;
	}

	public List<GuideReferenceEntry> referenceEntries() {
		return this.referenceEntries;
	}

	public GuideScenarioDefinition findScenario(String id) {
		return this.scenarios.stream()
			.filter(scenario -> scenario.id().equals(id))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown guide scenario: " + id));
	}

}
