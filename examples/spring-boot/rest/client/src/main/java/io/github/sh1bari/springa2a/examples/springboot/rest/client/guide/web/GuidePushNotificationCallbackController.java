package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.web;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session.GuidePushNotificationCallbackEvent;
import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session.GuidePushNotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guide/push-notifications")
public class GuidePushNotificationCallbackController {

	private final GuidePushNotificationInboxService inboxService;

	@PostMapping("/callback")
	public ResponseEntity<Void> receiveCallback(@RequestBody(required = false) String body) {
		this.inboxService.add(body);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/callback")
	public List<GuidePushNotificationCallbackEvent> listCallbacks() {
		return this.inboxService.snapshot();
	}

}
