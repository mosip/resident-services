package io.mosip.resident.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.dto.RIDOtpRequestDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;

@RestController
public class ResidentOtpController {

	@Autowired
	private ResidentOtpService residentOtpService;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private RequestValidator validator;

	@PostMapping(value = "/req/otp")
	public OtpResponseDTO reqOtp(@RequestBody OtpRequestDTO otpRequestDto) {
		audit.setAuditRequestDto(EventEnum.OTP_GEN);
		OtpResponseDTO otpResponseDTO = residentOtpService.generateOtp(otpRequestDto);
		audit.setAuditRequestDto(EventEnum.OTP_GEN_SUCCESS);
		return otpResponseDTO;
	}

	@PostMapping(value = "/req/rid-otp")
	public OtpResponseDTO reqRIDOtp(@RequestBody RIDOtpRequestDTO ridOtpRequestDto) 
			throws ResidentServiceCheckedException {
		audit.setAuditRequestDto(EventEnum.RID_OTP_GEN);
		validator.validateRIDOtpRequest(ridOtpRequestDto);
		OtpResponseDTO otpResponseDTO = residentOtpService.generateRIDOtp(ridOtpRequestDto);
		audit.setAuditRequestDto(EventEnum.RID_OTP_GEN_SUCCESS);
		return otpResponseDTO;
	}

}
