package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import io.mosip.resident.dto.MaskedResponseDTO;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.dto.RIDOtpRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.DataNotFoundException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.impl.ResidentOtpServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilitiy;

@RunWith(MockitoJUnitRunner.class)
public class ResidentOtpServiceTest {

	@InjectMocks
	private ResidentOtpServiceImpl residentOtpService;

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	private TokenGenerator tokenGenerator;

	@Mock
	Environment env;

	@Mock
	private AuditUtil audit;

	@Mock
    private Utilitiy utilitiy;

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
	public void testGenerateOtpSuccess() throws ApisResourceAccessException {
		when(residentServiceRestClient.postApi(any(), any(), any(), any(), any())).thenReturn(otpResponseDTO);
		
		OtpResponseDTO returnOtpResponseDTO = residentOtpService.generateOtp( new OtpRequestDTO());

		assertEquals(otpResponseDTO.getTransactionID(), returnOtpResponseDTO.getTransactionID());
		assertEquals(otpResponseDTO.getResponseTime(), returnOtpResponseDTO.getResponseTime());
		assertEquals(otpResponseDTO.getResponse().getMaskedEmail(), 
			returnOtpResponseDTO.getResponse().getMaskedEmail());
		assertEquals(otpResponseDTO.getResponse().getMaskedMobile(), 
			returnOtpResponseDTO.getResponse().getMaskedMobile());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGenerateOtpSuccessThowingApisResourceAccessException() throws ApisResourceAccessException {
		when(residentServiceRestClient.postApi(any(), any(), any(), any(), any()))
			.thenThrow(new ApisResourceAccessException());
		
		residentOtpService.generateOtp( new OtpRequestDTO());
	}

	@Test
	public void testGenerateRIDOtpSuccess() 
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		String uin = "123456789";
		Map<String,String> map = new HashMap<>();
		map.put("UIN", uin);
		when(utilitiy.retrieveIdrepoJson(anyString())).thenReturn(new JSONObject(map));
		when(residentServiceRestClient.postApi(any(), any(), any(), any(), any())).thenReturn(otpResponseDTO);
		
		RIDOtpRequestDTO ridOtpRequestDTO = new RIDOtpRequestDTO();
		ridOtpRequestDTO.setIndividualId("123456");
		OtpResponseDTO returnOtpResponseDTO = residentOtpService.generateRIDOtp(ridOtpRequestDTO);

		ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
		verify(residentServiceRestClient, times(1)).postApi(any(), any(), argumentCaptor.capture(), any(), any());
		OtpRequestDTO otpRequestDTO = (OtpRequestDTO) argumentCaptor.getValue();
		assertEquals(uin, otpRequestDTO.getIndividualId());
		assertEquals("UIN", otpRequestDTO.getIndividualIdType());
		assertEquals(otpResponseDTO.getTransactionID(), returnOtpResponseDTO.getTransactionID());
		assertEquals(otpResponseDTO.getResponseTime(), returnOtpResponseDTO.getResponseTime());
		assertEquals(otpResponseDTO.getResponse().getMaskedEmail(), 
			returnOtpResponseDTO.getResponse().getMaskedEmail());
		assertEquals(otpResponseDTO.getResponse().getMaskedMobile(), 
			returnOtpResponseDTO.getResponse().getMaskedMobile());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGenerateRIDOtpSuccessThowingApisResourceAccessException() 
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		Map<String,String> map = new HashMap<>();
		map.put("UIN", "123456789");
		when(utilitiy.retrieveIdrepoJson(anyString())).thenReturn(new JSONObject(map));
		when(residentServiceRestClient.postApi(any(), any(), any(), any(), any()))
			.thenThrow(new ApisResourceAccessException());
		
		RIDOtpRequestDTO ridOtpRequestDTO = new RIDOtpRequestDTO();
		ridOtpRequestDTO.setIndividualId("123456");
		residentOtpService.generateRIDOtp(ridOtpRequestDTO);
	}

	@Test(expected = DataNotFoundException.class)
	public void testGenerateRIDOtpSuccessThowingResidentServiceCheckedException() 
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		Map<String,String> map = new HashMap<>();
		map.put("UIN", "123456789");
		when(utilitiy.retrieveIdrepoJson(anyString())).thenThrow(new ResidentServiceCheckedException("123", "ResidentServiceCheckedException"));
		RIDOtpRequestDTO ridOtpRequestDTO = new RIDOtpRequestDTO();
		ridOtpRequestDTO.setIndividualId("123456");
		residentOtpService.generateRIDOtp(ridOtpRequestDTO);
	}

}
