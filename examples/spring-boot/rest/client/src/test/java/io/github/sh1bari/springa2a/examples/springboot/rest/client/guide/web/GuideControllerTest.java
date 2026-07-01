package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.web;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuidePageModel;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioDefinition;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.service.GuideService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuideController.class)
@DisplayName("Spring Boot REST guide controller")
class GuideControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GuideService guideService;

	@Test
	@DisplayName("renders the playground page from /guide")
	void rendersThePlaygroundPageFromGuide() throws Exception {
		when(this.guideService.loadPage(org.mockito.Mockito.any())).thenReturn(mockPage());

		this.mockMvc.perform(get("/guide"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Spring A2A Playground")));
	}

	@Test
	@DisplayName("renders the playground page from /guide/playground")
	void rendersThePlaygroundPageFromPlaygroundRoute() throws Exception {
		when(this.guideService.loadPage(org.mockito.Mockito.any())).thenReturn(mockPage());

		this.mockMvc.perform(get("/guide/playground"))
			.andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("Spring A2A Playground")));
	}

	private GuidePageModel mockPage() {
		return new GuidePageModel("http://localhost:18080", true, "Server reachable", "Client: running", "REST",
				"1.1.0.Final", "3.5.0", java.util.List.of(), null, null,
				"http://localhost:18081/guide/push-notifications/callback");
	}

}
