package io.github.sh1bari.springa2a.examples.springboot.rest.client.client;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideCallResult;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideTraceEvent;
import org.a2aproject.sdk.spec.AgentCard;

import java.util.List;

public interface GuideA2AClient {

	GuideCallResult discoverAgent() throws Exception;

	GuideCallResult sendMessage(AgentCard agentCard, String prompt) throws Exception;

	GuideCallResult inspectTask(AgentCard agentCard, String taskId, Integer historyLength) throws Exception;

	GuideCallResult streamMessage(AgentCard agentCard, String prompt, int timeoutSeconds,
			java.util.function.Consumer<GuideTraceEvent> traceConsumer) throws Exception;

	GuideCallResult listTasks(AgentCard agentCard, String contextId, String status, Integer pageSize, String pageToken,
			Integer historyLength, Boolean includeArtifacts) throws Exception;

	GuideCallResult cancelTask(AgentCard agentCard, String taskId) throws Exception;

	List<GuideCallResult> subscribeToTask(AgentCard agentCard, String taskId) throws Exception;

	GuideCallResult createTaskPushNotificationConfig(AgentCard agentCard, String taskId, String callbackUrl)
			throws Exception;

	GuideCallResult getTaskPushNotificationConfig(AgentCard agentCard, String taskId, String configId) throws Exception;

	GuideCallResult listTaskPushNotificationConfigs(AgentCard agentCard, String taskId, Integer pageSize,
			String pageToken) throws Exception;

	GuideCallResult deleteTaskPushNotificationConfig(AgentCard agentCard, String taskId, String configId)
			throws Exception;

}
