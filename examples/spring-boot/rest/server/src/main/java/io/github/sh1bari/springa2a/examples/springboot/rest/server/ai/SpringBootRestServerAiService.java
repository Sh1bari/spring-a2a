package io.github.sh1bari.springa2a.examples.springboot.rest.server.ai;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import io.github.sh1bari.springa2a.examples.springboot.rest.server.config.SpringBootRestServerOpenAiProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SpringBootRestServerAiService {

	private final ObjectProvider<ChatClient> chatClientProvider;

	private final SpringBootRestServerOpenAiProperties openAiProperties;

	public AiCallResult generateHelpNote(String input) {
		if (this.openAiProperties.apiKey() == null || this.openAiProperties.apiKey().isBlank()) {
			return AiCallResult.mock("Mock response from LLM");
		}

		String prompt = """
				You are the helper for a Spring A2A REST demo.
				Explain what the demo can do in one short, practical sentence.
				If the input is useful, weave it into the answer:
				%s
				""".formatted(input == null ? "" : input);

		try {
			ChatClient client = this.chatClientProvider.getIfAvailable();
			if (client == null) {
				return AiCallResult.mock("Mock response from LLM");
			}
			String content = client.prompt().user(prompt).call().content();
			return AiCallResult.generated(content);
		}
		catch (Exception ex) {
			return AiCallResult.mock("Mock response from LLM");
		}
	}

	public record AiCallResult(boolean generated, String content, String fallbackReason) {

		static AiCallResult generated(String content) {
			return new AiCallResult(true, content, null);
		}

		static AiCallResult mock(String content) {
			return new AiCallResult(false, content, "ChatClient is not configured or the model call failed");
		}

	}

}
