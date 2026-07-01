package io.github.sh1bari.springa2a.examples.springboot.rest.client.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(info = @Info(title = "A2A Spring Boot REST Client Demo", version = "1.0.0", description = """
		Scenario-based demo app for the A2A Spring Boot REST integration.

		Start with the overview endpoint, then run blocking and streaming scenarios.
		Use /demo/agent-card only when you want the raw discovery payload.
		"""), servers = @Server(url = "http://localhost:18081"), tags = { @Tag(name = "A2A Spring Boot REST Demo",
		description = "Scenario endpoints for a compact A2A REST client walkthrough") })
public class SpringBootRestClientOpenApiConfig {

}
