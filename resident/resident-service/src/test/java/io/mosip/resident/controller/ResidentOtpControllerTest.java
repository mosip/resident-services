package io.mosip.resident.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.mosip.idrepository.core.util.EnvUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.IndividualIdResponseDto;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.handler.service.UinCardRePrintService;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(EnvUtil.class)
@ActiveProfiles("test")
public class ResidentOtpControllerTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	private ResidentOtpService residentOtpService;

	@MockBean
	private RequestValidator validator;

	@MockBean
	private ResidentUpdateService residentUpdateService;

	@MockBean
	private IdAuthService idAuthService;

	@MockBean
	private ResidentVidService vidService;

	@MockBean
	private DocumentService docService;

	@MockBean
	private ObjectStoreHelper objectStore;

	@MockBean
	private NotificationService notificationService;

	@MockBean
	private UinCardRePrintService rePrintService;

	@MockBean
	private Utility utility;

	@MockBean
	private Utilities utilities;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;
	
	@MockBean
    private ResidentServiceImpl residentService;

	@Mock
	private AuditUtil audit;

	@InjectMocks
	ResidentOtpController residentOtpController;

	@Autowired
	private MockMvc mockMvc;

	Gson gson = new GsonBuilder().serializeNulls().create();

	String reqJson;

	OtpResponseDTO otpResponseDTO;

	OtpRequestDTO otpRequestDTO;

	@Before
	public void setup() throws Exception {
		otpResponseDTO = new OtpResponseDTO();
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(residentOtpController).build();
		otpRequestDTO = new OtpRequestDTO();
		otpRequestDTO.setIndividualId("123456");
		otpRequestDTO.setTransactionID("1234327890");
		reqJson = gson.toJson(otpRequestDTO);
		ReflectionTestUtils.setField(residentOtpController, "otpRequestId", "mosip.identity.otp.internal");
		ReflectionTestUtils.setField(residentOtpController, "otpRequestVersion", "1.0");
	}

	@Test
	public void testCreateRequestGenerationSuccess() throws Exception {
		Mockito.when(residentOtpService.generateOtp(Mockito.any())).thenReturn(otpResponseDTO);
		mockMvc.perform(
				MockMvcRequestBuilders.post("/req/otp").contentType(MediaType.APPLICATION_JSON_VALUE).content(reqJson))
				.andExpect(status().isOk());
	}

	@Test(expected = Exception.class)
	public void testCreateRequestGenerationWithResidentServiceException() throws Exception {
		Mockito.when(residentOtpService.generateOtp(Mockito.any())).thenThrow(ResidentServiceException.class);
		mockMvc.perform(
				MockMvcRequestBuilders.post("/req/otp").contentType(MediaType.APPLICATION_JSON_VALUE).content(reqJson))
				.andExpect(status().isOk());
	}

	@Test
	public void reqOtpForAidTest() throws Exception {
		IndividualIdOtpRequestDTO individualIdOtpRequestDTO = new IndividualIdOtpRequestDTO();
		individualIdOtpRequestDTO.setIndividualId("123456789");
		IndividualIdResponseDto individualIdResponseDto = new IndividualIdResponseDto();
		individualIdResponseDto.setTransactionId("12345678");
		Mockito.when(residentOtpService.generateOtpForIndividualId(individualIdOtpRequestDTO)).thenReturn(individualIdResponseDto);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(individualIdOtpRequestDTO);
		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/individualId/otp").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test(expected = Exception.class)
	public void reqOtpForAidTestResidentServiceCheckedException() throws Exception {
		IndividualIdOtpRequestDTO individualIdOtpRequestDTO = new IndividualIdOtpRequestDTO();
		individualIdOtpRequestDTO.setIndividualId("123456789");
		IndividualIdResponseDto individualIdResponseDto = new IndividualIdResponseDto();
		individualIdResponseDto.setTransactionId("12345678");
		Mockito.when(residentOtpService.generateOtpForIndividualId(individualIdOtpRequestDTO)).thenThrow(new ResidentServiceCheckedException("res-ser", "error thrown"));
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(individualIdOtpRequestDTO);
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/individualId/otp").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test(expected = Exception.class)
	public void reqOtpForAidTestInvalidInputException() throws Exception {
		doThrow(new InvalidInputException()).when(validator).validateReqOtp(any());
		IndividualIdOtpRequestDTO individualIdOtpRequestDTO = new IndividualIdOtpRequestDTO();
		individualIdOtpRequestDTO.setIndividualId("123456789");
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(individualIdOtpRequestDTO);
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/individualId/otp").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("resident")
	public void reqOtpForAidNullTest() throws Exception {
		ReflectionTestUtils.setField(residentOtpController, "otpRequestId", "id");
		IndividualIdOtpRequestDTO aidOtpRequestDTO = new IndividualIdOtpRequestDTO();
		aidOtpRequestDTO.setIndividualId(null);
		IndividualIdResponseDto individualIdResponseDto = new IndividualIdResponseDto();
		Mockito.when(residentOtpService.generateOtpForIndividualId(Mockito.any())).thenReturn(individualIdResponseDto);
		assertNotNull(residentOtpController.reqOtpForIndividualId(aidOtpRequestDTO));
	}
}
