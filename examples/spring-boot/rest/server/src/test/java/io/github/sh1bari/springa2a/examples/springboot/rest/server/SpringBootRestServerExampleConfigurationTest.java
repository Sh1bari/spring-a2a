package io.github.sh1bari.springa2a.examples.springboot.rest.server;

import io.github.sh1bari.springa2a.examples.springboot.rest.server.ai.SpringBootRestServerAiService;
import io.github.sh1bari.springa2a.examples.springboot.rest.server.config.SpringBootRestServerExampleConfiguration;
import io.github.sh1bari.springa2a.examples.springboot.rest.server.config.SpringBootRestServerOpenAiProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Spring Boot REST server example configuration")
class SpringBootRestServerExampleConfigurationTest {

	@Test
	@DisplayName("enables streaming and push notifications")
	void enablesStreamingAndPushNotifications() {
		SpringBootRestServerExampleConfiguration configuration = new SpringBootRestServerExampleConfiguration();

		var agentCard = configuration.agentCard();

		assertTrue(agentCard.capabilities().streaming());
		assertTrue(agentCard.capabilities().pushNotifications());
		assertTrue(agentCard.skills().get(0).examples().contains("cancel this"));
	}

	@Test
	@DisplayName("falls back to a mock LLM response when chat client is unavailable")
	void fallsBackToMockResponseWhenChatClientIsUnavailable() {
		ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
		when(chatClientProvider.getIfAvailable()).thenReturn(null);

		SpringBootRestServerAiService aiService = new SpringBootRestServerAiService(chatClientProvider,
				new SpringBootRestServerOpenAiProperties(""));

		SpringBootRestServerAiService.AiCallResult result = aiService.generateHelpNote("hello");

		assertThat(result.generated()).isFalse();
		assertThat(result.content()).isEqualTo("Mock response from LLM");
	}

}
