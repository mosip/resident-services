package io.mosip.resident.test.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.mosip.resident.controller.ResidentOtpController;
import io.mosip.resident.dto.MaskedResponseDTO;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.dto.RIDOtpRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;

@RunWith(MockitoJUnitRunner.class)
public class ResidentOtpControllerTest {

	@InjectMocks
	ResidentOtpController residentOtpController;

	@Mock
	private ResidentOtpService residentOtpService;

	@Mock
	private AuditUtil audit;

	@Mock
	private RequestValidator validator;

	private OtpResponseDTO otpResponseDTO;

	@Before
	public void setup() throws IOException, ApisResourceAccessException {
		otpResponseDTO = new OtpResponseDTO();
		otpResponseDTO.setTransactionID("1234");
		otpResponseDTO.setResponseTime("2022-04-18T21:45:16Z");
		MaskedResponseDTO maskedResponseDTO = new MaskedResponseDTO();
		maskedResponseDTO.setMaskedEmail("abcd@1234");
		maskedResponseDTO.setMaskedMobile("maskedMobile");
		otpResponseDTO.setResponse(maskedResponseDTO);
	}

	@Test
	public void reqOtpSuccessTest() throws ApisResourceAccessException {
		when(residentOtpService.generateOtp(any())).thenReturn(otpResponseDTO);

		OtpResponseDTO returnOtpResponseDTO = residentOtpController.reqOtp(new OtpRequestDTO());

		assertEquals(otpResponseDTO.getTransactionID(), returnOtpResponseDTO.getTransactionID());
		assertEquals(otpResponseDTO.getResponseTime(), returnOtpResponseDTO.getResponseTime());
		assertEquals(otpResponseDTO.getResponse().getMaskedEmail(), 
			returnOtpResponseDTO.getResponse().getMaskedEmail());
		assertEquals(otpResponseDTO.getResponse().getMaskedMobile(), 
			returnOtpResponseDTO.getResponse().getMaskedMobile());
		verify(audit, times(2)).setAuditRequestDto(any());
	}

	@Test
	public void reqRIDOtpSuccessTest() throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(residentOtpService.generateRIDOtp(any())).thenReturn(otpResponseDTO);
		
		OtpResponseDTO returnOtpResponseDTO = null;
		try {
			returnOtpResponseDTO = residentOtpController.reqRIDOtp(new RIDOtpRequestDTO());
		} catch(Exception e) {
			e.printStackTrace();
		}

		assertEquals(otpResponseDTO.getTransactionID(), returnOtpResponseDTO.getTransactionID());
		assertEquals(otpResponseDTO.getResponseTime(), returnOtpResponseDTO.getResponseTime());
		assertEquals(otpResponseDTO.getResponse().getMaskedEmail(), 
			returnOtpResponseDTO.getResponse().getMaskedEmail());
		assertEquals(otpResponseDTO.getResponse().getMaskedMobile(), 
			returnOtpResponseDTO.getResponse().getMaskedMobile());
		verify(audit, times(2)).setAuditRequestDto(any());
	}

}
