package io.github.sh1bari.springa2a.examples.springboot.rest.server.agent;

import lombok.extern.slf4j.Slf4j;
import org.a2aproject.sdk.server.agentexecution.AgentExecutor;
import org.a2aproject.sdk.server.agentexecution.RequestContext;
import org.a2aproject.sdk.server.tasks.AgentEmitter;
import org.a2aproject.sdk.spec.TaskState;
import org.a2aproject.sdk.spec.TextPart;
import org.a2aproject.sdk.spec.UnsupportedOperationError;
import org.springframework.stereotype.Component;

import io.github.sh1bari.springa2a.examples.springboot.rest.server.ai.SpringBootRestServerAiService;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SpringBootRestServerAgentExecutor implements AgentExecutor {

	private final SpringBootRestServerAiService aiService;

	private final Set<String> cancelableTaskIds = ConcurrentHashMap.newKeySet();

	private static final int CALLBACK_DEMO_SECONDS = 20;

	public SpringBootRestServerAgentExecutor(SpringBootRestServerAiService aiService) {
		this.aiService = aiService;
	}

	@Override
	public void execute(RequestContext context, AgentEmitter agentEmitter) {
		String input = context.getUserInput("\n");
		log.info("AgentExecutor received input: {}", input);

		String normalizedInput = input == null ? "" : input.toLowerCase(Locale.ROOT);

		if (normalizedInput.contains("help")) {
			log.info("Returning capabilities overview");
			SpringBootRestServerAiService.AiCallResult aiCallResult = aiService.generateHelpNote(input);
			String message = aiCallResult.generated()
					? "This demo understands hello, stream, and help. Model-backed help: " + aiCallResult.content()
					: aiCallResult.content();
			agentEmitter.sendMessage(message);
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

		if (normalizedInput.contains("/task")) {
			log.info("Running predictable task demo");
			agentEmitter.submit();
			agentEmitter.startWork();
			agentEmitter.addArtifact(List.of(new TextPart("Task artifact from Spring Boot REST")));
			agentEmitter.complete();
			return;
		}

		if (normalizedInput.contains("cancel")) {
			log.info("Running cancellable task demo");
			cancelableTaskIds.add(agentEmitter.getTaskId());
			agentEmitter.submit();
			agentEmitter.startWork();
			agentEmitter.requiresInput(agentEmitter.newAgentMessage(
					List.of(new TextPart("This task is intentionally left open so the client can cancel it")),
					context.getMessage() != null ? context.getMessage().metadata() : java.util.Map.of()));
			return;
		}

		if (normalizedInput.contains("/callback-demo") || normalizedInput.contains("callback demo")) {
			log.info("Running push callback demo");
			agentEmitter.submit();
			try {
				Thread.sleep(3000L);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				agentEmitter.cancel(agentEmitter.newAgentMessage(List.of(new TextPart("Callback demo interrupted")),
						context.getMessage() != null ? context.getMessage().metadata() : java.util.Map.of()));
				return;
			}
			agentEmitter.startWork();
			for (int second = 1; second <= CALLBACK_DEMO_SECONDS; second++) {
				if (Thread.currentThread().isInterrupted()) {
					log.info("Callback demo interrupted after {} seconds", second - 1);
					agentEmitter
						.cancel(agentEmitter.newAgentMessage(List.of(new TextPart("Callback demo was interrupted")),
								context.getMessage() != null ? context.getMessage().metadata() : java.util.Map.of()));
					return;
				}
				try {
					Thread.sleep(1000L);
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					agentEmitter.cancel(agentEmitter.newAgentMessage(
							List.of(new TextPart("Callback demo interrupted while sleeping")),
							context.getMessage() != null ? context.getMessage().metadata() : java.util.Map.of()));
					return;
				}
				agentEmitter.updateStatus(TaskState.TASK_STATE_WORKING,
						agentEmitter.newAgentMessage(
								List.of(new TextPart("Callback demo tick " + second + " of " + CALLBACK_DEMO_SECONDS)),
								context.getMessage() != null ? context.getMessage().metadata() : java.util.Map.of()));
			}
			agentEmitter.complete(agentEmitter.newAgentMessage(
					List.of(new TextPart("Callback demo finished after " + CALLBACK_DEMO_SECONDS + " seconds")),
					context.getMessage() != null ? context.getMessage().metadata() : java.util.Map.of()));
			return;
		}

		log.info("Returning direct message response");
		agentEmitter.sendMessage(
				"Hello from Spring Boot REST. Ask for 'help' to see available prompts, or 'stream' to see streaming.");
	}

	@Override
	public void cancel(RequestContext context, AgentEmitter agentEmitter) {
		log.info("Cancel requested for task {}", context.getTaskId());
		if (cancelableTaskIds.remove(context.getTaskId())) {
			agentEmitter.cancel();
			return;
		}
		throw new UnsupportedOperationError();
	}

}
