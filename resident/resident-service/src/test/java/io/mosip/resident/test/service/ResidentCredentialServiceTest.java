package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import io.mosip.resident.dto.CryptomanagerResponseDto;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.dto.EncryptResponseDto;
import io.mosip.resident.dto.RIDDigitalCardRequestDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.DataNotFoundException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilitiy;

@RunWith(MockitoJUnitRunner.class)
public class ResidentCredentialServiceTest {

	private static final String AVAILABLE = "available";

	private static final String NOT_AVAILABLE = "notAvailable";

	@InjectMocks
	private ResidentCredentialServiceImpl residentCredentialService;

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

	@Mock
	IdAuthService idAuthService;

	@Mock
	private ObjectMapper mapper;

	private ResponseWrapper<DigitalCardStatusResponseDto> digitalCardStatusResponseWrapper;
	
	private CryptomanagerResponseDto cryptomanagerResponseDto;

	private RIDDigitalCardRequestDto ridDigitalCardRequestDto;

	@Before
	public void setup() throws IOException, ApisResourceAccessException {
		digitalCardStatusResponseWrapper = new ResponseWrapper<DigitalCardStatusResponseDto>();
		DigitalCardStatusResponseDto digitalCardStatusResponseDto = new DigitalCardStatusResponseDto();
		digitalCardStatusResponseDto.setId("10001090900001020220414054750");
		digitalCardStatusResponseDto.setStatusCode(AVAILABLE);
		digitalCardStatusResponseDto.setUrl("http://datashare/1234");
		digitalCardStatusResponseWrapper.setResponse(digitalCardStatusResponseDto);

		EncryptResponseDto encryptResponseDto = new EncryptResponseDto();
		encryptResponseDto.setData("YWJjZGVm");
		cryptomanagerResponseDto = new CryptomanagerResponseDto();
		cryptomanagerResponseDto.setResponse(encryptResponseDto);

		ridDigitalCardRequestDto = new RIDDigitalCardRequestDto();
		ridDigitalCardRequestDto.setIndividualId("10001090900001020220414054750");
		ridDigitalCardRequestDto.setOtp("123456");
		ridDigitalCardRequestDto.setTransactionID("123456789");
	}

	@Test
	public void testGetRIDDigitalCardSuccess() 
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException, JsonMappingException, JsonProcessingException {
		String uin = "123456789";
		Map<String,String> map = new HashMap<>();
		map.put("UIN", uin);
		when(utilitiy.retrieveIdrepoJson(anyString())).thenReturn(new JSONObject(map));
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(true);
		when(residentServiceRestClient.getApi(any(), eq(ResponseWrapper.class), any()))
			.thenReturn(digitalCardStatusResponseWrapper);
		when(residentServiceRestClient.getApi(any(), eq(String.class), any()))
			.thenReturn("YWJjZGVm");
		RIDDigitalCardRequestDto ridDigitalCardRequestDto = new RIDDigitalCardRequestDto();
		ridDigitalCardRequestDto.setIndividualId("10001090900001020220414054750");
		ridDigitalCardRequestDto.setOtp("123456");
		ridDigitalCardRequestDto.setTransactionID("123456789");

		byte[] pdfBytes = residentCredentialService.getRIDDigitalCard(ridDigitalCardRequestDto);

		assertEquals("abcdef", new String(pdfBytes));
	}

	@Test(expected = DataNotFoundException.class)
	public void testGetRIDDigitalCardThowingResidentServiceCheckedException() 
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		when(utilitiy.retrieveIdrepoJson(anyString())).thenThrow(new ResidentServiceCheckedException("123", "ResidentServiceCheckedException"));
		RIDDigitalCardRequestDto ridDigitalCardRequestDto = new RIDDigitalCardRequestDto();
		ridDigitalCardRequestDto.setIndividualId("10001090900001020220414054750");
		ridDigitalCardRequestDto.setOtp("123456");
		ridDigitalCardRequestDto.setTransactionID("123456789");
		residentCredentialService.getRIDDigitalCard(ridDigitalCardRequestDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetRIDDigitalCardOTPAuthenticationFailed() 
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException {
		String uin = "123456789";
		Map<String,String> map = new HashMap<>();
		map.put("UIN", uin);
		when(utilitiy.retrieveIdrepoJson(anyString())).thenReturn(new JSONObject(map));
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(false);
		residentCredentialService.getRIDDigitalCard(ridDigitalCardRequestDto);
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetRIDDigitalCardNotAvailable() 
			throws ApisResourceAccessException, ResidentServiceCheckedException, OtpValidationFailedException, JsonMappingException, JsonProcessingException {
		String uin = "123456789";
		Map<String,String> map = new HashMap<>();
		map.put("UIN", uin);
		when(utilitiy.retrieveIdrepoJson(anyString())).thenReturn(new JSONObject(map));
		when(idAuthService.validateOtp(anyString(), anyString(), anyString())).thenReturn(true);
		digitalCardStatusResponseWrapper.getResponse().setStatusCode(NOT_AVAILABLE);
		when(residentServiceRestClient.getApi(any(), eq(ResponseWrapper.class), any()))
			.thenReturn(digitalCardStatusResponseWrapper);
		RIDDigitalCardRequestDto ridDigitalCardRequestDto = new RIDDigitalCardRequestDto();
		ridDigitalCardRequestDto.setIndividualId("10001090900001020220414054750");
		ridDigitalCardRequestDto.setOtp("123456");
		ridDigitalCardRequestDto.setTransactionID("123456789");

		residentCredentialService.getRIDDigitalCard(ridDigitalCardRequestDto);
	}
}
