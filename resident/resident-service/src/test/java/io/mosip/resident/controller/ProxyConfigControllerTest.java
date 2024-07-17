package io.mosip.resident.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import io.mosip.idrepository.core.util.EnvUtil;
import io.mosip.resident.util.Utility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.ResidentConfigServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import org.springframework.web.context.WebApplicationContext;

/**
 * Resident proxy config controller test class.
 *
 * @author Ritik Jain
 */
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(EnvUtil.class)
@ActiveProfiles("test")
public class ProxyConfigControllerTest {

	@MockBean
	private ProxyIdRepoService proxyIdRepoService;

	@MockBean
	private ResidentConfigServiceImpl residentConfigService;

	@MockBean
	private AuditUtil auditUtil;

	@MockBean
	private Utility utilityBean;

	@InjectMocks
	private ProxyConfigController proxyConfigController;

	@MockBean
	private DocumentController documentController;

	@MockBean
	private IdAuthController idAuthController;

	@MockBean
	private IdentityController identityController;

	@MockBean
	private ResidentController residentController;

	@MockBean
	private ResidentVidService vidService;

	@MockBean
	private ResidentServiceImpl residentService;

	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@MockBean
	private ObjectStoreHelper objectStore;

	@Autowired
	private MockMvc mockMvc;

	private ResponseWrapper responseWrapper;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(proxyConfigController).build();
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any());
	}

	@Test
	public void testGetResidentProperties() throws Exception {
		Mockito.when(residentConfigService.getUIProperties()).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/proxy/config/ui-properties")).andExpect(status().isOk());
	}

	@Test
	public void testGetResidentUISchema() throws Exception {
		Mockito.when(residentConfigService.getUISchema("update-demographics")).thenReturn("ui-schema-json");
		mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/config/ui-schema/update-demographics")).andExpect(status().isOk());
	}

	@Test(expected = Exception.class)
	public void testGetResidentUISchemaWithResidentServiceException() throws Exception {
		Mockito.when(residentConfigService.getUISchema("update-demographics")).thenThrow(ResidentServiceException.class);
		mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/config/ui-schema/update-demographics")).andExpect(status().isOk());
	}

	@Test
	public void testGetIdentityMapping() throws Exception {
		Mockito.when(residentConfigService.getIdentityMapping()).thenReturn("identity-mapping-json");
		mockMvc.perform(MockMvcRequestBuilders.get("/auth-proxy/config/identity-mapping")).andExpect(status().isOk());
	}

}