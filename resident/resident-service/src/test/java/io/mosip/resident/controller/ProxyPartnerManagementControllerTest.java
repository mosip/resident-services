package io.mosip.resident.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.mosip.resident.util.Utility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.ProxyPartnerManagementServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;

/**
 * Resident proxy partner management controller test class.
 * 
 * @author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
public class ProxyPartnerManagementControllerTest {
	
    @Mock
    private ProxyIdRepoService proxyIdRepoService;

	@Mock
	private ProxyPartnerManagementServiceImpl proxyPartnerManagementService;

	@Mock
	private AuditUtil auditUtil;

	@Mock
	private Utility utilityBean;

	@Mock
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@InjectMocks
	private ProxyPartnerManagementController proxyPartnerManagementController;

	@Mock
	private DocumentService docService;

	@Mock
	private ObjectStoreHelper objectStore;

	@Mock
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

	@Mock
	private ResidentVidService vidService;
	
	@Mock
    private ResidentServiceImpl residentService;

	@Mock
	private Environment env;

	@Autowired
	private MockMvc mockMvc;

	private ResponseWrapper responseWrapper;

	@Before
	public void setUp() throws Exception {
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(proxyPartnerManagementController).build();
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any());
	}

	@Test
	public void testGetPartnersByPartnerType() throws Exception {
		Mockito.when(proxyPartnerManagementService.getPartnersByPartnerType(Mockito.any())).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/partners?partnerType=")).andExpect(status().isOk());
	}

	@Test(expected = Exception.class)
	public void testGetPartnersByPartnerTypeWithException() throws Exception {
		Mockito.when(env.getProperty(Mockito.anyString())).thenReturn("property");
		Mockito.when(proxyPartnerManagementService.getPartnersByPartnerType(Mockito.any()))
				.thenThrow(new ResidentServiceCheckedException());
		mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/partners?partnerType=")).andExpect(status().isOk());
	}
}
