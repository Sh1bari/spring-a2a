package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session;

import java.time.Instant;

public record GuidePushNotificationCallbackEvent(Instant timestamp, String body) {
}
