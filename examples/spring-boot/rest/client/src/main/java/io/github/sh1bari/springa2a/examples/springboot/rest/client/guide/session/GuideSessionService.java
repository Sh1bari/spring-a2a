package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class GuideSessionService {

	private static final String SESSION_ATTRIBUTE = GuideSessionState.class.getName();

	private final GuidePushNotificationInboxService inboxService;

	public GuideSessionService(GuidePushNotificationInboxService inboxService) {
		this.inboxService = inboxService;
	}

	public GuideSessionState getOrCreate(HttpSession session) {
		GuideSessionState state = (GuideSessionState) session.getAttribute(SESSION_ATTRIBUTE);
		if (state == null) {
			state = new GuideSessionState();
			session.setAttribute(SESSION_ATTRIBUTE, state);
		}
		return state;
	}

	public void reset(HttpSession session) {
		GuideSessionState state = getOrCreate(session);
		state.reset();
		this.inboxService.clear();
	}

}
