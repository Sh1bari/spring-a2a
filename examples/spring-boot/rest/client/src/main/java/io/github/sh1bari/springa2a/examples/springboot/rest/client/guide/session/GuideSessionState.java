package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioOutcome;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GuideSessionState implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String currentScenarioId;

	private String currentTaskId;

	private String currentContextId;

	private String currentMessageId;

	private String currentPushNotificationConfigId;

	private String agentCardJson;

	private String agentCardName;

	private final List<GuideScenarioOutcome> history = new ArrayList<>();

	private final Set<String> completedScenarios = new LinkedHashSet<>();

	public String getCurrentScenarioId() {
		return this.currentScenarioId;
	}

	public void setCurrentScenarioId(String currentScenarioId) {
		this.currentScenarioId = currentScenarioId;
	}

	public String getCurrentTaskId() {
		return this.currentTaskId;
	}

	public void setCurrentTaskId(String currentTaskId) {
		this.currentTaskId = currentTaskId;
	}

	public String getCurrentContextId() {
		return this.currentContextId;
	}

	public void setCurrentContextId(String currentContextId) {
		this.currentContextId = currentContextId;
	}

	public String getCurrentMessageId() {
		return this.currentMessageId;
	}

	public void setCurrentMessageId(String currentMessageId) {
		this.currentMessageId = currentMessageId;
	}

	public String getCurrentPushNotificationConfigId() {
		return this.currentPushNotificationConfigId;
	}

	public void setCurrentPushNotificationConfigId(String currentPushNotificationConfigId) {
		this.currentPushNotificationConfigId = currentPushNotificationConfigId;
	}

	public String getAgentCardJson() {
		return this.agentCardJson;
	}

	public void setAgentCardJson(String agentCardJson) {
		this.agentCardJson = agentCardJson;
	}

	public String getAgentCardName() {
		return this.agentCardName;
	}

	public void setAgentCardName(String agentCardName) {
		this.agentCardName = agentCardName;
	}

	public List<GuideScenarioOutcome> getHistory() {
		return this.history;
	}

	public Set<String> getCompletedScenarios() {
		return this.completedScenarios;
	}

	public void markCompleted(String scenarioId) {
		this.completedScenarios.add(scenarioId);
		this.currentScenarioId = scenarioId;
	}

	public void record(GuideScenarioOutcome outcome) {
		this.history.add(0, outcome);
		if (this.history.size() > 10) {
			this.history.remove(this.history.size() - 1);
		}
		this.currentScenarioId = outcome.scenarioId();
		this.currentTaskId = outcome.currentTaskId();
		this.currentContextId = outcome.currentContextId();
	}

	public void reset() {
		this.currentScenarioId = null;
		this.currentTaskId = null;
		this.currentContextId = null;
		this.currentMessageId = null;
		this.currentPushNotificationConfigId = null;
		this.agentCardJson = null;
		this.agentCardName = null;
		this.history.clear();
		this.completedScenarios.clear();
	}

}
