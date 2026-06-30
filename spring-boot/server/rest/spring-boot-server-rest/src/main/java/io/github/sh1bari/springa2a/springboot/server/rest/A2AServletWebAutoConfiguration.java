package io.github.sh1bari.springa2a.springboot.server.rest;

import io.github.sh1bari.springa2a.springboot.server.autoconfigure.A2ARuntimeAutoConfiguration;
import io.github.sh1bari.springa2a.springboot.server.autoconfigure.A2ASpringBootProperties;
import org.a2aproject.sdk.server.requesthandlers.RequestHandler;
import org.a2aproject.sdk.spec.AgentCard;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configures the servlet-based A2A transport adapter.
 *
 * <p>
 * This configuration is only active in servlet web applications and only contributes the
 * MVC-layer beans: the response mapper, exception handler, request mapper, and
 * controller. The core runtime beans are provided separately by
 * {@link A2ARuntimeAutoConfiguration}.
 */
@AutoConfiguration(after = A2ARuntimeAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class A2AServletWebAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public A2ASpringBootHttpResponseMapper a2aSpringBootHttpResponseMapper() {
		return new A2ASpringBootHttpResponseMapper();
	}

	@Bean
	@ConditionalOnMissingBean
	public A2ASpringBootMvcExceptionHandler a2aSpringBootMvcExceptionHandler(
			A2ASpringBootHttpResponseMapper responseMapper) {
		return new A2ASpringBootMvcExceptionHandler(responseMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public A2APushNotificationConfigRequestMapper a2aPushNotificationConfigRequestMapper() {
		return new A2APushNotificationConfigRequestMapper();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean({ AgentCard.class, RequestHandler.class })
	public A2ASpringBootMvcController a2aSpringBootMvcController(@Qualifier("agentCard") AgentCard agentCard,
			@Qualifier("extendedAgentCard") ObjectProvider<AgentCard> extendedAgentCard,
			ObjectProvider<A2ASpringBootProperties> properties, RequestHandler requestHandler,
			A2ASpringBootHttpResponseMapper responseMapper,
			A2APushNotificationConfigRequestMapper pushNotificationConfigRequestMapper,
			ObjectProvider<StreamingSubscriptionObserver> streamingSubscriptionObserver) {
		return new A2ASpringBootMvcController(agentCard, extendedAgentCard.getIfAvailable(),
				properties.getIfAvailable(A2ASpringBootProperties::new), requestHandler, responseMapper,
				pushNotificationConfigRequestMapper, streamingSubscriptionObserver);
	}

}
