package io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.web;

import io.github.sh1bari.springa2a.examples.springboot.rest.client.guide.service.GuideService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class GuideController {

	private final GuideService guideService;

	@GetMapping("/guide")
	public String guide(Model model, HttpSession session) {
		model.addAttribute("page", this.guideService.loadPage(session));
		return "guide/playground";
	}

	@GetMapping("/guide/playground")
	public String playground(Model model, HttpSession session) {
		model.addAttribute("page", this.guideService.loadPage(session));
		return "guide/playground";
	}

}
