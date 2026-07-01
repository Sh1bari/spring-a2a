package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.service;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.config.SpringBootRestClientExampleProperties;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.client.GuideA2AClient;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideCallResult;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuidePageModel;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioOutcome;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioRequest;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.scenario.GuideScenarioCatalog;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.scenario.GuidePlaygroundTemplateCatalog;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session.GuideSessionService;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session.GuideSessionState;
import org.a2aproject.sdk.jsonrpc.common.json.JsonUtil;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentInterface;
import org.a2aproject.sdk.spec.AgentSkill;
import org.a2aproject.sdk.spec.TransportProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpSession;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Spring Boot REST guide service")
class GuideServiceTest {

	@Test
	@DisplayName("loads the guide page with a reachable server")
	void loadsTheGuidePageWithAReachableServer() throws Exception {
		SpringBootRestClientExampleProperties properties = new SpringBootRestClientExampleProperties(
				"http://localhost:18080", "hello", "stream this", 15,
				"http://localhost:18081/guide/push-notifications/callback");
		GuideA2AClient guideA2AClient = Mockito.mock(GuideA2AClient.class);
		GuideSessionService sessionService = Mockito.mock(GuideSessionService.class);
		GuideSessionState sessionState = new GuideSessionState();
		HttpSession session = Mockito.mock(HttpSession.class);

		when(sessionService.getOrCreate(session)).thenReturn(sessionState);
		when(guideA2AClient.discoverAgent()).thenReturn(mockDiscovery());

		GuideService service = new GuideService(properties, guideA2AClient, new GuideScenarioCatalog(),
				new GuidePlaygroundTemplateCatalog(properties), sessionService);

		GuidePageModel page = service.loadPage(session);

		assertThat(page.serverAvailable()).isTrue();
		assertThat(page.playgroundTemplates()).hasSizeGreaterThan(0);
	}

	@Test
	@DisplayName("stores a successful scenario outcome in session state")
	void storesASuccessfulScenarioOutcomeInSessionState() throws Exception {
		SpringBootRestClientExampleProperties properties = new SpringBootRestClientExampleProperties(
				"http://localhost:18080", "hello", "stream this", 15,
				"http://localhost:18081/guide/push-notifications/callback");
		GuideA2AClient guideA2AClient = Mockito.mock(GuideA2AClient.class);
		GuideSessionService sessionService = Mockito.mock(GuideSessionService.class);
		GuideSessionState sessionState = new GuideSessionState();
		HttpSession session = Mockito.mock(HttpSession.class);

		when(sessionService.getOrCreate(session)).thenReturn(sessionState);
		when(guideA2AClient.discoverAgent()).thenReturn(mockDiscovery());
		when(guideA2AClient.sendMessage(any(), any())).thenReturn(mockCall("Message", "Message: hello", null, null));

		GuideService service = new GuideService(properties, guideA2AClient, new GuideScenarioCatalog(),
				new GuidePlaygroundTemplateCatalog(properties), sessionService);

		GuideScenarioOutcome outcome = service.runScenario("send-message",
				new GuideScenarioRequest("hello", 15, null, null, null, null, null, null, null, null, null), session);

		assertThat(outcome.success()).isTrue();
		assertThat(sessionState.getHistory()).hasSize(1);
		assertThat(sessionState.getCurrentScenarioId()).isEqualTo("send-message");
	}

	private GuideCallResult mockDiscovery() throws Exception {
		AgentCard agentCard = AgentCard.builder()
			.name("Demo Agent")
			.description("Demo")
			.version("1.0.0")
			.capabilities(AgentCapabilities.builder().streaming(true).build())
			.supportedInterfaces(
					List.of(new AgentInterface(TransportProtocol.HTTP_JSON.asString(), "http://localhost:18080")))
			.defaultInputModes(List.of("text"))
			.defaultOutputModes(List.of("text"))
			.skills(List.of(AgentSkill.builder()
				.id("demo")
				.name("Demo")
				.description("Demo")
				.tags(List.of("guide"))
				.examples(List.of("hello"))
				.build()))
			.build();
		return new GuideCallResult("Discover Agent", "GET", "/.well-known/agent-card.json", null, null, "AgentCard",
				"Discovered agent Demo Agent", JsonUtil.toJson(agentCard), null, null, null, null, List.of(), true, 200,
				null);
	}

	private GuideCallResult mockCall(String responseType, String humanReadable, String taskId, String contextId) {
		return new GuideCallResult("Send Message", "POST", "/message:send", "{\"prompt\":\"hello\"}", "curl -X POST",
				responseType, humanReadable, "{\"text\":\"hello\"}", "No task yet", "Direct message response", taskId,
				contextId, List.of(), true, 200, null);
	}

}
