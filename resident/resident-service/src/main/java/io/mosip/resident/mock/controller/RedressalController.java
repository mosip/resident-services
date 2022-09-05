package io.mosip.resident.mock.controller;

import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/mock")

@Controller
public class RedressalController {
	@Value("${mosip.resident.griveance}")
	private String griveance;

	@RequestMapping("/external/grievence/redressel")
	public String grievence(Model model, @RequestParam("name") String name, @RequestParam("emailId") String emailId,
			@RequestParam("phoneNo") String phoneNo, @RequestParam("eventId") String eventId)
			throws MalformedURLException {
		String griveancevalue = griveance;
		model.addAttribute("name", name);
		model.addAttribute("email", emailId);
		model.addAttribute("phoneno", phoneNo);
		model.addAttribute("eventId", eventId);
		return "griveance";
	}
}
