package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.web;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideCallResult;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideErrorView;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuidePageModel;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioOutcome;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioRequest;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideTraceEvent;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.service.GuideService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuideApiController.class)
@DisplayName("Spring Boot REST guide API controller")
class GuideApiControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GuideService guideService;

	@Test
	@DisplayName("runs a scenario and returns JSON")
	void runsAScenarioAndReturnsJson() throws Exception {
		when(this.guideService.runScenario(any(), any(), any())).thenReturn(mockOutcome());

		this.mockMvc
			.perform(post("/guide/scenarios/discover-agent/run").contentType(MediaType.APPLICATION_JSON)
				.content("{\"prompt\":\"hello\"}"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("\"scenarioId\":\"discover-agent\"")));
	}

	@Test
	@DisplayName("resets the guide session")
	void resetsTheGuideSession() throws Exception {
		when(this.guideService.reset(any())).thenReturn(mockPage());

		this.mockMvc.perform(post("/guide/reset"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("\"serverAvailable\":true")));
	}

	private GuideScenarioOutcome mockOutcome() {
		GuideCallResult step = new GuideCallResult("Discover Agent", "GET", "/.well-known/agent-card.json", null, null,
				"AgentCard", "Discovered agent", "{\"name\":\"demo\"}", null, null, null, null,
				List.of(new GuideTraceEvent(Instant.parse("2026-07-01T00:00:00Z"), "client", "Discovery request",
						null)),
				true, 200, null);
		return new GuideScenarioOutcome("discover-agent", "Discover Agent", true, "http://localhost:18080",
				List.of(step), "Agent Card discovered", List.of("Discovery"), List.of("Agent Card"), null, null,
				List.of(step.traceEvents().get(0)), null);
	}

	private GuidePageModel mockPage() {
		return new GuidePageModel("http://localhost:18080", true, "Server reachable", "Client: running", "REST",
				"1.1.0.Final", "3.5.0", java.util.List.of(), null, null,
				"http://localhost:18081/guide/push-notifications/callback");
	}

}
