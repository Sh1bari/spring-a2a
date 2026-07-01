package io.github.sh1bari.springa2a.examples.springboot.rest.server;

import org.springframework.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration;

@SpringBootApplication(
		exclude = { OpenAiAudioSpeechAutoConfiguration.class, OpenAiAudioTranscriptionAutoConfiguration.class,
				OpenAiChatAutoConfiguration.class, OpenAiEmbeddingAutoConfiguration.class,
				OpenAiImageAutoConfiguration.class, OpenAiModerationAutoConfiguration.class })
public class SpringBootRestServerExampleApplication {

	public static void main(String[] args) {
		String apiKey = System.getenv("OPENROUTER_API_KEY");
		boolean hasApiKey = StringUtils.hasText(apiKey);
		System.setProperty("spring.ai.model.chat", hasApiKey ? "openai" : "disabled");
		System.setProperty("spring.ai.model.audio.speech", "disabled");
		System.setProperty("spring.ai.model.audio.transcription", "disabled");
		SpringApplication.run(SpringBootRestServerExampleApplication.class, args);
	}

}
