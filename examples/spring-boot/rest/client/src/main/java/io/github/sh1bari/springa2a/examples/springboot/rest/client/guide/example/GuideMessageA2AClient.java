package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.example;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideCallResult;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideTraceEvent;
import org.a2aproject.sdk.spec.AgentCard;

import java.util.function.Consumer;

public interface GuideMessageA2AClient {

	GuideCallResult sendMessage(AgentCard agentCard, String prompt) throws Exception;

	GuideCallResult streamMessage(AgentCard agentCard, String prompt, int timeoutSeconds,
			Consumer<GuideTraceEvent> traceConsumer) throws Exception;

}
