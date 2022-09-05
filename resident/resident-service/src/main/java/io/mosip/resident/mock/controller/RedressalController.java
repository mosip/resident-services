package io.mosip.resident.mock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/external")

@Controller
public class RedressalController {
	@RequestMapping("/grievence/redressel")
	@ResponseBody
	public String welcome() {
		return "griveance.html";
	}
}
