package io.github.sh1bari.springa2a.examples.springboot.rest.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.dto.SpringBootRestClientDemoOverviewResponse;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.dto.SpringBootRestClientDemoRequest;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.dto.SpringBootRestClientFullFlowResponse;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.dto.SpringBootRestClientScenarioResponse;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.service.SpringBootRestClientDemoService;
import org.a2aproject.sdk.spec.AgentCard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/demo")
@Tag(name = "A2A Spring Boot REST Demo",
		description = "Scenario endpoints for discovery, blocking, streaming, and a combined end-to-end walkthrough")
public class SpringBootRestClientDemoController {

	private final SpringBootRestClientDemoService demoService;

	@GetMapping
	@Operation(summary = "Inspect the demo flow",
			description = "Returns the available scenarios and sample prompts. Use /demo/agent-card for the raw discovery payload.")
	public ResponseEntity<SpringBootRestClientDemoOverviewResponse> getDemoOverview() {
		return ResponseEntity.ok(demoService.describeDemo());
	}

	@GetMapping("/agent-card")
	@Operation(summary = "Fetch the remote agent card",
			description = "Calls the server example and returns its AgentCard as-is.")
	public ResponseEntity<AgentCard> fetchAgentCard() {
		return ResponseEntity.ok(demoService.fetchAgentCard());
	}

	@PostMapping("/blocking")
	@Operation(summary = "Run the blocking message flow",
			description = "Sends one blocking message and returns the observed events.")
	public ResponseEntity<SpringBootRestClientScenarioResponse> runBlocking(
			@RequestBody(required = false) SpringBootRestClientDemoRequest request) {
		log.info("Running blocking demo endpoint");
		return ResponseEntity.ok(demoService.runBlockingDemo(request));
	}

	@PostMapping("/streaming")
	@Operation(summary = "Run the streaming message flow",
			description = "Sends one streaming message and returns the observed task events.")
	public ResponseEntity<SpringBootRestClientScenarioResponse> runStreaming(
			@RequestBody(required = false) SpringBootRestClientDemoRequest request) {
		log.info("Running streaming demo endpoint");
		return ResponseEntity.ok(demoService.runStreamingDemo(request));
	}

	@PostMapping("/help")
	@Operation(summary = "Run the help message flow",
			description = "Sends a help request through A2A and returns the model-backed response.")
	public ResponseEntity<SpringBootRestClientScenarioResponse> runHelp(
			@RequestBody(required = false) SpringBootRestClientDemoRequest request) {
		log.info("Running help demo endpoint");
		return ResponseEntity.ok(demoService.runHelpDemo());
	}

	@PostMapping("/full-flow")
	@Operation(summary = "Run the full demo flow",
			description = "Runs the blocking and streaming flows and returns a combined report without embedding the card.")
	public ResponseEntity<SpringBootRestClientFullFlowResponse> runFullFlow(
			@RequestBody(required = false) SpringBootRestClientDemoRequest request) {
		log.info("Running full-flow demo endpoint");
		return ResponseEntity.ok(demoService.runFullFlow(request));
	}

}
