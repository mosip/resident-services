package io.mosip.resident.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import io.mosip.resident.util.Utility;
import org.junit.Before;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.IdAuthRequestDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdAuthServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import reactor.util.function.Tuples;

/**
 * Resident IdAuth controller test class.
 * 
 * @author Ritik Jain
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
public class IdAuthControllerTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	private IdAuthServiceImpl idAuthService;

	@Mock
	private AuditUtil auditUtil;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;
	
	@MockBean
	private ResidentVidService vidService;

	@InjectMocks
	private IdAuthController idAuthController;

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private DocumentService docService;
	
	@MockBean
	private ObjectStoreHelper objectStore;
	
	@MockBean
    private ResidentServiceImpl residentService;

	@MockBean
	private Utility utilityBean;

	Gson gson = new GsonBuilder().serializeNulls().create();

	String reqJson;
	private RequestWrapper<IdAuthRequestDto> requestWrapper;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(idAuthController).build();
		requestWrapper = new RequestWrapper<IdAuthRequestDto>();
		IdAuthRequestDto idAuthRequestDto = new IdAuthRequestDto();
		idAuthRequestDto.setTransactionId("1234567890");
		idAuthRequestDto.setIndividualId("8251649601");
		idAuthRequestDto.setOtp("111111");
		requestWrapper.setRequest(idAuthRequestDto);
		reqJson = gson.toJson(requestWrapper);
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any());
		ReflectionTestUtils.setField(idAuthController, "validateOtpId", "validate-otp-id");
	}

	@Test
	public void testValidateOtp() throws Exception {
		Mockito.when(idAuthService.validateOtpV1(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Tuples.of(true, "12345"));
		mockMvc.perform(MockMvcRequestBuilders.post("/validate-otp").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(reqJson.getBytes())).andExpect(status().isOk());
	}

	@Test(expected = OtpValidationFailedException.class)
	public void testValidateOtpFailed() throws Exception {
		Mockito.doThrow(new OtpValidationFailedException("otp validation failed", Map.of(ResidentConstants.EVENT_ID, "123456"))).when(idAuthService)
				.validateOtpV1(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		idAuthController.validateOtp(requestWrapper);
	}

}
