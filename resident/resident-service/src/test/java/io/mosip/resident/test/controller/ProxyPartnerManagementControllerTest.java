package io.mosip.resident.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.controller.ProxyPartnerManagementController;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;

/**
 * Resident proxy partner management controller test class.
 * 
 * @author Ritik Jain
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
public class ProxyPartnerManagementControllerTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	private ProxyPartnerManagementService proxyPartnerManagementService;

	@Mock
	private AuditUtil auditUtil;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@InjectMocks
	private ProxyPartnerManagementController proxyPartnerManagementController;

	@MockBean
	private DocumentService docService;

	@MockBean
	private ObjectStoreHelper objectStore;

	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

	@MockBean
	private ResidentVidService vidService;

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

}
