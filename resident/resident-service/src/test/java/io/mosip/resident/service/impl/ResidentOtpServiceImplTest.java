package io.mosip.resident.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.resident.dto.AuthError;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.IndividualIdResponseDto;
import io.mosip.resident.dto.MaskedResponseDTO;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;

/**
 * 
 * @author M1063027 Rama devi
 *
 */

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentOtpServiceImplTest {

	@Mock
	private ResidentServiceRestClient residentServiceRestClient;

	@Mock
	Environment env;

	@Mock
	private AuditUtil audit;

	@Mock
	private IdentityServiceImpl identityServiceImpl;

	@Mock
	private ResidentTransactionRepository residentTransactionRepository;

	@Mock
	private ResidentServiceImpl residentServiceImpl;

	@Mock
	private Utility utility;

	@InjectMocks
	private ResidentOtpServiceImpl residentOtpServiceImpl;
	
	@Mock
	private ObjectMapper objectMapper;

	@Before
	public void setUp() throws Exception {
		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		OtpResponseDTO responseDto = getOtpResponseDTO();
		responseDto.setTransactionID("1232323232");

		when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseDto);
		IndividualIdOtpRequestDTO aidOtpRequestDTO = getAidOtpRequestDTO();
		aidOtpRequestDTO.setIndividualId("9054257143");
		aidOtpRequestDTO.setOtpChannel(List.of("EMAIL", "PHONE"));
		Mockito.when(identityServiceImpl.getIndividualIdForAid(any())).thenReturn("9054257143");
		Mockito.when(objectMapper.convertValue(aidOtpRequestDTO, OtpRequestDTO.class)).thenReturn(otpRequestDTO);
	}

	@Test
	public void generateOtpMailTest() throws Exception {
		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		otpRequestDTO.setOtpChannel(List.of("EMAIL"));
		assertNotNull(residentOtpServiceImpl.generateOtp(otpRequestDTO));
	}

	@Test
	public void generateOtpPhoneTest() throws Exception {
		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		IdentityDTO identityDTO = getIdentityDTO();
		identityDTO.setEmail(null);
		otpRequestDTO.setOtpChannel(List.of("EMAIL"));
		assertNotNull(residentOtpServiceImpl.generateOtp(otpRequestDTO));
	}


	@Test
	public void generateOtpForIndividualId() throws Exception {
		IndividualIdOtpRequestDTO aidOtpRequestDTO = getAidOtpRequestDTO();
		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		otpRequestDTO.setIndividualId("9054257143");
		otpRequestDTO.setOtpChannel(List.of("EMAIL", "PHONE"));
		aidOtpRequestDTO.setIndividualId("9054257143");
		aidOtpRequestDTO.setOtpChannel(List.of("EMAIL", "PHONE"));
		Mockito.when(identityServiceImpl.getIndividualIdForAid(any())).thenReturn("9054257143");
		Mockito.when(objectMapper.convertValue(aidOtpRequestDTO, OtpRequestDTO.class)).thenReturn(otpRequestDTO);
		OtpResponseDTO otpResponseDTO = new OtpResponseDTO();
		otpResponseDTO.setResponse(new MaskedResponseDTO());
		otpResponseDTO.setTransactionID("1234567789");
		IndividualIdResponseDto individualIdResponseDto = new IndividualIdResponseDto();
		individualIdResponseDto.setResponse(new MaskedResponseDTO());
		individualIdResponseDto.setTransactionId("1234567789");
		residentOtpServiceImpl.generateOtpForIndividualId(aidOtpRequestDTO);
	}

	@Ignore
	@Test(expected = ResidentServiceCheckedException.class)
	public void generateOtpFailureTest() throws Exception {
		IndividualIdOtpRequestDTO aidOtpRequestDTO = getAidOtpRequestDTO();
		OtpRequestDTO otpRequestDTO = getOtpRequestDTO();
		IdentityDTO identityDTO = getIdentityDTO();
		identityDTO.setEmail(null);
		identityDTO.setPhone(null);
		OtpResponseDTO otpResponseDTO = new OtpResponseDTO();
		otpResponseDTO.setResponse(new MaskedResponseDTO());

		Mockito.when(identityServiceImpl.getIdentity(otpRequestDTO.getIndividualId())).thenReturn(identityDTO);
		when(identityServiceImpl.getIndividualIdForAid(any())).thenReturn(otpRequestDTO.getIndividualId());
		Mockito.when(residentOtpServiceImpl.generateOtp(any())).thenThrow(new ResidentServiceCheckedException());
		Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(otpResponseDTO);
		assertNotNull(residentOtpServiceImpl.generateOtpForIndividualId(aidOtpRequestDTO));
	}

	private IndividualIdOtpRequestDTO getAidOtpRequestDTO() {
		IndividualIdOtpRequestDTO aidOtpRequestDTO = new IndividualIdOtpRequestDTO();
		aidOtpRequestDTO.setIndividualId("individualId");
		return aidOtpRequestDTO;
	}

	private OtpResponseDTO getOtpResponseDTO() {
		OtpResponseDTO responseDto = new OtpResponseDTO();
		List<AuthError> errors = new ArrayList<AuthError>();
		responseDto.setErrors(errors);
		MaskedResponseDTO maskedResponseDTO = new MaskedResponseDTO();
		maskedResponseDTO.setMaskedEmail("r***********47@gmail.com");
		maskedResponseDTO.setMaskedMobile("12*****89");
		responseDto.setResponse(maskedResponseDTO);

		/*
		 * AuthError error1 = new AuthError("RES-SER-425",
		 * "while generating otp error is occured"); AuthError error2 = new
		 * AuthError(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
		 * ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage());
		 * responseDto.setErrors(errors); errors.add(error1); errors.add(error2);
		 */
		return responseDto;
	}

	private OtpRequestDTO getOtpRequestDTO() {
		OtpRequestDTO otpRequestDTO = new OtpRequestDTO();
		otpRequestDTO.setIndividualId("individualId");
		otpRequestDTO.setTransactionID("transactionID");
		return otpRequestDTO;
	}

	private IdentityDTO getIdentityDTO() {
		IdentityDTO identityDTO = new IdentityDTO();
		identityDTO.setEmail("email");
		identityDTO.setUIN("UIN");
		identityDTO.setPhone("phone");
		return identityDTO;

	}

}
