package io.github.sh1bari.springa2a.examples.springboot.rest.server;

import lombok.extern.slf4j.Slf4j;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TextPart;
import org.a2aproject.sdk.spec.UnsupportedOperationError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class SpringBootRestServerAgentExecutor implements AgentExecutor {

	@Override
	public void execute(RequestContext context, AgentEmitter agentEmitter) {
		String input = context.getUserInput("\n");
		log.info("AgentExecutor received input: {}", input);

		String normalizedInput = input == null ? "" : input.toLowerCase(Locale.ROOT);

		if (normalizedInput.contains("help")) {
			log.info("Returning capabilities overview");
			agentEmitter.sendMessage(
					"This demo understands hello, stream, and help. Try a message containing 'stream' to see task updates.");
			return;
		}

		if (normalizedInput.contains("stream")) {
			log.info("Running streaming task demo");
			agentEmitter.submit();
			agentEmitter.startWork();
			agentEmitter.addArtifact(List.of(new TextPart("Streaming artifact from Spring Boot REST")));
			agentEmitter.complete();
			return;
		}

		log.info("Returning direct message response");
		agentEmitter.sendMessage(
				"Hello from Spring Boot REST. Ask for 'help' to see available prompts, or 'stream' to see streaming.");
	}

	@Override
	public void cancel(RequestContext context, AgentEmitter agentEmitter) {
		log.info("Cancel requested for task {}", context.getTaskId());
		throw new UnsupportedOperationError();
	}

}
