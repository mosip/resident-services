package io.mosip.resident.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.IndividualIdResponseDto;
import io.mosip.resident.dto.MaskedResponseDTO;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;

/**
 * 
 * @author Rama devi
 *
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentOtpServiceTest {

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	Environment env;

	@InjectMocks
	private ResidentOtpService residentOtpService = new ResidentOtpServiceImpl();

	@Mock
	private IdentityServiceImpl identityServiceImpl;

	@Mock
	private Utility utility;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	private ResidentTransactionEntity residentTransactionEntity;

	@Before
	public void setup() throws Exception {
		String otpAPIUrl = "https://dev2.mosip.net/idauthentication/v1/internal/otp";
		when(env.getProperty(ApiName.OTP_GEN_URL.name())).thenReturn(otpAPIUrl);
		residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setRequestTypeCode(RequestType.SEND_OTP.name());
		when(utility.createEntity(any())).thenReturn(residentTransactionEntity);
		when(utility.createEventId()).thenReturn("1122334455667788");
	}

	@Test
	public void testGenerateOtp()
			throws ApisResourceAccessException, ResidentServiceCheckedException, NoSuchAlgorithmException {
		OtpResponseDTO otpResponseDTO = getOtpResponseDTO();
		when(residentServiceRestClient.postApi(anyString(), any(), any(), any())).thenReturn(otpResponseDTO);
		when(utility.getIdForResidentTransaction(anyString(), anyList())).thenReturn("hash ref id");

		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		otpRequestDTO.setOtpChannel(List.of("EMAIL"));
		residentOtpService.generateOtp(otpRequestDTO);

		verify(residentServiceRestClient, times(1)).postApi(anyString(), any(), any(), any(Class.class));
		verify(env, times(1)).getProperty(ApiName.OTP_GEN_URL.name());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGenerateOtpWithApisResourceAccessException()
			throws ApisResourceAccessException, ResidentServiceCheckedException, NoSuchAlgorithmException {
		when(residentServiceRestClient.postApi(anyString(), any(), any(), any(Class.class)))
				.thenThrow(new ApisResourceAccessException());
		residentOtpService.generateOtp(new OtpRequestDTO());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGenerateOtpWithResidentServiceCheckedException()
			throws ApisResourceAccessException, ResidentServiceCheckedException, NoSuchAlgorithmException {
		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		otpRequestDTO.setOtpChannel(List.of("EMAIL"));
		OtpResponseDTO otpResponseDTO = getOtpResponseDTO();
		when(residentServiceRestClient.postApi(anyString(), any(), any(), any())).thenReturn(otpResponseDTO);
		when(utility.getIdForResidentTransaction(anyString(), anyList()))
				.thenThrow(new ResidentServiceCheckedException());
		residentOtpService.generateOtp(otpRequestDTO);
	}

	@Test
	public void testGenerateOtpForIndividualId()
			throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
		IndividualIdOtpRequestDTO individualIdOtpRequestDTO = getIndividualIdOtpRequestDTO();
		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		when(identityServiceImpl.getIndividualIdForAid(any())).thenReturn("9054257143");
		when(objectMapper.convertValue(individualIdOtpRequestDTO, OtpRequestDTO.class)).thenReturn(otpRequestDTO);
		OtpResponseDTO otpResponseDTO = getOtpResponseDTO();
		when(residentServiceRestClient.postApi(anyString(), any(), any(), any())).thenReturn(otpResponseDTO);
		IndividualIdResponseDto individualIdResponseDto = getIndividualIdResponseDto();
		when(objectMapper.convertValue(otpResponseDTO, IndividualIdResponseDto.class))
				.thenReturn(individualIdResponseDto);
		residentOtpService.generateOtpForIndividualId(individualIdOtpRequestDTO);
	}

	@Test(expected = ResidentServiceCheckedException.class)
	public void testGenerateOtpForIndividualIdWithApisResourceAccessException()
			throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException {
		IndividualIdOtpRequestDTO individualIdOtpRequestDTO = getIndividualIdOtpRequestDTO();
		when(identityServiceImpl.getIndividualIdForAid(any())).thenThrow(new ApisResourceAccessException());
		residentOtpService.generateOtpForIndividualId(individualIdOtpRequestDTO);
	}

	private OtpRequestDTO getOtpRequestDTO() {
		OtpRequestDTO otpRequestDTO = new OtpRequestDTO();
		otpRequestDTO.setIndividualId("9054257143");
		otpRequestDTO.setOtpChannel(List.of("EMAIL", "PHONE"));
		return otpRequestDTO;
	}

	private OtpResponseDTO getOtpResponseDTO() {
		OtpResponseDTO responseDto = new OtpResponseDTO();
		MaskedResponseDTO maskedResponseDTO = new MaskedResponseDTO();
		maskedResponseDTO.setMaskedEmail("r***********47@gmail.com");
		maskedResponseDTO.setMaskedMobile("12*****89");
		responseDto.setResponse(maskedResponseDTO);
		responseDto.setTransactionID("1232323232");
		return responseDto;
	}

	private IndividualIdOtpRequestDTO getIndividualIdOtpRequestDTO() {
		IndividualIdOtpRequestDTO individualIdOtpRequestDTO = new IndividualIdOtpRequestDTO();
		individualIdOtpRequestDTO.setOtpChannel(List.of("EMAIL", "PHONE"));
		individualIdOtpRequestDTO.setTransactionId("1232323232");
		return individualIdOtpRequestDTO;
	}

	private IndividualIdResponseDto getIndividualIdResponseDto() {
		IndividualIdResponseDto individualIdResponseDto = new IndividualIdResponseDto();
		MaskedResponseDTO maskedResponseDTO = new MaskedResponseDTO();
		maskedResponseDTO.setMaskedEmail("r***********47@gmail.com");
		maskedResponseDTO.setMaskedMobile("12*****89");
		individualIdResponseDto.setResponse(maskedResponseDTO);
		return individualIdResponseDto;
	}
}