package io.github.sh1bari.springa2a.examples.springboot.rest.server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("Spring Boot REST server example application")
class SpringBootRestServerExampleApplicationTest {

	@Test
	@DisplayName("starts without an OpenRouter API key")
	void startsWithoutAnOpenRouterApiKey() {
	}

}
