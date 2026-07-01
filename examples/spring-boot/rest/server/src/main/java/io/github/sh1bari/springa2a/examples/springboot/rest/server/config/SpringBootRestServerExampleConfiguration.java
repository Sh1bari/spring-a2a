package io.github.sh1bari.springa2a.examples.springboot.rest.server.config;

import lombok.extern.slf4j.Slf4j;
import org.a2aproject.sdk.spec.AgentCapabilities;
import org.a2aproject.sdk.spec.AgentCard;
import org.a2aproject.sdk.spec.AgentInterface;
import org.a2aproject.sdk.spec.AgentSkill;
import org.a2aproject.sdk.spec.TransportProtocol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class SpringBootRestServerExampleConfiguration {

	@Bean
	public AgentCard agentCard() {
		log.info("Creating Spring Boot REST example agent card");
		return AgentCard.builder()
			.name("Spring Boot REST Demo Agent")
			.description(
					"A small Spring Boot agent that demonstrates discovery, direct replies, streaming task updates, push notification config management, and callback delivery")
			.supportedInterfaces(
					List.of(new AgentInterface(TransportProtocol.HTTP_JSON.asString(), "http://localhost:18080")))
			.version("1.0.0")
			.capabilities(AgentCapabilities.builder().streaming(true).pushNotifications(true).build())
			.defaultInputModes(List.of("text"))
			.defaultOutputModes(List.of("text"))
			.skills(List.of(AgentSkill.builder()
				.id("spring_boot_rest_demo")
				.name("Spring Boot REST demo")
				.description(
						"Replies to hello messages, streams artifacts for stream prompts, and exposes push notification config management")
				.tags(List.of("spring-boot", "rest", "example", "push-notifications"))
				.examples(List.of("hello", "stream this", "help", "cancel this", "subscribe to this"))
				.build()))
			.build();
	}

}
