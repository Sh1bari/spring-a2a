package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GuidePushNotificationInboxService {

	private final List<GuidePushNotificationCallbackEvent> callbacks = new ArrayList<>();

	public synchronized void add(String body) {
		this.callbacks.add(0,
				new GuidePushNotificationCallbackEvent(Instant.now(), body == null || body.isBlank() ? "{}" : body));
		if (this.callbacks.size() > 20) {
			this.callbacks.remove(this.callbacks.size() - 1);
		}
	}

	public synchronized List<GuidePushNotificationCallbackEvent> snapshot() {
		return List.copyOf(this.callbacks);
	}

	public synchronized void clear() {
		this.callbacks.clear();
	}

}
