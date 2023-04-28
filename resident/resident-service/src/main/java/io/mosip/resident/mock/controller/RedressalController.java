package io.mosip.resident.mock.controller;

import java.net.MalformedURLException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "redressal-controller", description = "Redressal Controller")
public class RedressalController {

	private static final Logger logger = LoggerConfiguration.logConfig(RedressalController.class);

	@GetMapping("/mock/external/grievance/redressel")
	public String grievence(Model model, @RequestParam("name") String name, @RequestParam("emailId") String emailId,
			@RequestParam("phoneNo") String phoneNo, @RequestParam("eventId") String eventId)
			throws MalformedURLException {

		model.addAttribute("name", name);
		model.addAttribute("email", emailId);
		model.addAttribute("phoneno", phoneNo);
		model.addAttribute("eventId", eventId);
		return "grievance";
	}

	@ResponseFilter
	@GetMapping(value = "/auth-mock/order/physical-card")
	@Operation(summary = "getPaymentStatus", description = "getPaymentStatus", tags = { "redressal-controller" })
	public String getPaymentStatus(Model model, @RequestParam("redirectUrl") String redirectUrl,
			@RequestParam("eventId") String eventId, @RequestParam("residentName") String residentName,
			@RequestParam("residentDefaultFullAddress") String residentDefaultFullAddress) {
		logger.debug("RedressalController::getPaymentStatus()::entry");
		String url = new String(Base64.decodeBase64(redirectUrl.getBytes()));
		model.addAttribute("redirectUrl", url);
		model.addAttribute("eventId", eventId);
		model.addAttribute("residentName", residentName);
		model.addAttribute("residentFullAddress", residentDefaultFullAddress);
		logger.debug("RedressalController::getPaymentStatus()::exit");
		return "payment-page";
	}
}
