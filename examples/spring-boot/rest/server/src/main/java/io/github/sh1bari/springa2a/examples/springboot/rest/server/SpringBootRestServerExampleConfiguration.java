package io.github.sh1bari.springa2a.examples.springboot.rest.server;

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
			.name("Spring Boot REST Example Agent")
			.description("Minimal Spring Boot example for the A2A REST transport")
			.supportedInterfaces(
					List.of(new AgentInterface(TransportProtocol.HTTP_JSON.asString(), "http://localhost:18080")))
			.version("1.0.0")
			.capabilities(AgentCapabilities.builder().streaming(true).pushNotifications(false).build())
			.defaultInputModes(List.of("text"))
			.defaultOutputModes(List.of("text"))
			.skills(List.of(AgentSkill.builder()
				.id("spring_boot_rest_example")
				.name("Spring Boot REST example")
				.description("Responds with a short fixed message")
				.tags(List.of("spring-boot", "rest", "example"))
				.examples(List.of("hello"))
				.build()))
			.build();
	}

}
