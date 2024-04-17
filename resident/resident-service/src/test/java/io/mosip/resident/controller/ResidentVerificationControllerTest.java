package io.mosip.resident.controller;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;

/**
 * Resident Verification Controller Test Note: This class is used to test the
 * Resident Verification Controller
 * 
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ResidentVerificationControllerTest {

	@MockBean
	private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	private VerificationService verificationService;

	@MockBean
	private IdAuthService idAuthService;

	@MockBean
	private ResidentVidService vidService;

	@MockBean
	private Utility utilityBean;

	@MockBean
	private DocumentService docService;

	@MockBean
	private ObjectStoreHelper objectStore;

	@MockBean
	private RequestValidator requestValidator;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@MockBean
	private ResidentServiceImpl residentService;

	@Mock
	private AuditUtil audit;

	@InjectMocks
	VerificationController verificationController;

	@Autowired
	private MockMvc mockMvc;

	VerificationResponseDTO verificationResponseDTO;

	@Before
	public void setup() throws Exception {
		verificationResponseDTO = new VerificationResponseDTO();
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(verificationController).build();
	}

	@Test
	public void testCreateRequestGenerationSuccess() throws Exception {
		Mockito.when(verificationService.checkChannelVerificationStatus(Mockito.any(), Mockito.any()))
				.thenReturn(verificationResponseDTO);
		mockMvc.perform(
				MockMvcRequestBuilders.get("/channel/verification-status/?channel=EMAIL&individualId=8251649601"))
				.andExpect(status().isOk());
	}

	@Test(expected = Exception.class)
	public void testGetChannelVerificationStatusWithException() throws Exception {
		doThrow(new ResidentServiceCheckedException()).when(verificationService)
				.checkChannelVerificationStatus(Mockito.anyString(), Mockito.anyString());
		mockMvc.perform(
				MockMvcRequestBuilders.get("/channel/verification-status/?channel=EMAIL&individualId=8251649601"))
				.andExpect(status().isOk());
	}
}
