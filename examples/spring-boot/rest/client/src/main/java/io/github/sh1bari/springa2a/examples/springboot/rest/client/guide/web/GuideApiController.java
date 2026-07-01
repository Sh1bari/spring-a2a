package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.web;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuidePageModel;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioOutcome;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideScenarioRequest;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.model.GuideTraceEvent;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.service.GuideService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.util.function.Consumer;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guide")
public class GuideApiController {

	private final GuideService guideService;

	private final TaskExecutor guideTaskExecutor;

	@PostMapping("/reset")
	public GuidePageModel reset(HttpSession session) {
		return this.guideService.reset(session);
	}

	@PostMapping("/scenarios/{scenarioId}/run")
	public GuideScenarioOutcome runScenario(@PathVariable String scenarioId,
			@org.springframework.web.bind.annotation.RequestBody(required = false) GuideScenarioRequest request,
			HttpSession session) {
		return this.guideService.runScenario(scenarioId, request, session);
	}

	@GetMapping("/scenarios/{scenarioId}/stream")
	public SseEmitter streamScenario(@PathVariable String scenarioId, @RequestParam(required = false) String prompt,
			@RequestParam(required = false) Integer streamingTimeoutSeconds, HttpSession session) {
		GuideScenarioRequest request = new GuideScenarioRequest(prompt, streamingTimeoutSeconds, null, null, null, null,
				null, null, null, null, null);
		SseEmitter emitter = new SseEmitter(0L);
		Consumer<GuideTraceEvent> traceSink = event -> {
			try {
				emitter.send(SseEmitter.event().name("trace").data(event));
			}
			catch (IOException ex) {
				throw new IllegalStateException("Unable to stream guide trace event", ex);
			}
		};
		this.guideTaskExecutor.execute(() -> {
			try {
				GuideScenarioOutcome outcome = this.guideService.runScenario(scenarioId, request, session, traceSink);
				emitter.send(SseEmitter.event().name("result").data(outcome));
				emitter.complete();
			}
			catch (IOException ex) {
				emitter.completeWithError(ex);
			}
			catch (Exception ex) {
				emitter.completeWithError(ex);
			}
		});
		return emitter;
	}

}
