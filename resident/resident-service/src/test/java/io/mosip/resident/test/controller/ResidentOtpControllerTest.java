package io.mosip.resident.test.controller;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.resident.controller.ResidentOtpController;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.IndividualIdResponseDto;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
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
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
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

	@Test
	public void createRequestGenerationSuccessTest() throws Exception {
		Mockito.when(residentOtpService.generateOtp(otpRequestDTO)).thenReturn(otpResponseDTO);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(otpRequestDTO);
		this.mockMvc
				.perform(MockMvcRequestBuilders.post("/req/otp").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());// .andExpect(jsonPath("$.response.vid", is("12345")))
	}
	
	@Ignore
	@Test
	public void reqOtpForAidTest() throws Exception {
		IndividualIdOtpRequestDTO individualIdOtpRequestDTO = new IndividualIdOtpRequestDTO();
		individualIdOtpRequestDTO.setIndividualId("123456789");
		Mockito.when(residentOtpService.generateOtp(otpRequestDTO)).thenReturn(otpResponseDTO);
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(individualIdOtpRequestDTO);
		this.mockMvc.perform(
				MockMvcRequestBuilders.post("/individualId/otp").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());// .andExpect(jsonPath("$.response.vid", is("12345")))
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
