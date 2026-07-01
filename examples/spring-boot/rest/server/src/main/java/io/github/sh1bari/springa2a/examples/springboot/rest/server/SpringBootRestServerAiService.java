package io.github.sh1bari.springa2a.examples.springboot.rest.server;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SpringBootRestServerAiService {

	private final ChatClient chatClient;

	private final SpringBootRestServerOpenAiProperties openAiProperties;

	public AiCallResult generateHelpNote(String input) {
		if (this.openAiProperties.apiKey() == null || this.openAiProperties.apiKey().isBlank()) {
			return AiCallResult.fallback("OPENROUTER_API_KEY is not configured");
		}

		String prompt = """
				You are the helper for a Spring A2A REST demo.
				Explain what the demo can do in one short, practical sentence.
				If the input is useful, weave it into the answer:
				%s
				""".formatted(input == null ? "" : input);

		String content = this.chatClient.prompt().user(prompt).call().content();
		return AiCallResult.generated(content);
	}

	public record AiCallResult(boolean generated, String content, String fallbackReason) {

		static AiCallResult generated(String content) {
			return new AiCallResult(true, content, null);
		}

		static AiCallResult fallback(String fallbackReason) {
			return new AiCallResult(false, null, fallbackReason);
		}

	}

}
