package io.mosip.resident.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;

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
import io.mosip.resident.controller.DocumentController;
import io.mosip.resident.controller.IdAuthController;
import io.mosip.resident.controller.IdentityController;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;

/**
 * Resident identity controller test class.
 * 
 * @author Ritik Jain
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
public class IdentityControllerTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

	@InjectMocks
	private IdentityController identityController;

	@MockBean
	private IdentityServiceImpl idServiceImpl;

	@MockBean
	private ResidentVidService vidService;

	@Mock
	private AuditUtil auditUtil;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@MockBean
	private DocumentController documentController;

	@MockBean
	private IdAuthController idAuthController;

	@MockBean
	private ObjectStoreHelper objectStore;

	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
    private ResidentServiceImpl residentService;

	private ResponseWrapper responseWrapper;

	private Map identityMap;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(identityController).build();
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any());

		identityMap = new LinkedHashMap();
		identityMap.put("UIN", "8251649601");
		identityMap.put("email", "manojvsp12@gmail.com");
		identityMap.put("phone", "9395910872");
		identityMap.put("dateOfBirth", "1970/11/16");

		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		responseWrapper.setResponse(identityMap);
	}

	@Test
	public void testGetInputAttributeValues() throws Exception {
		Mockito.when(idServiceImpl.getIdentityAttributes(Mockito.anyString(),Mockito.anyString())).thenReturn(identityMap);
		mockMvc.perform(MockMvcRequestBuilders.get("/identity/info/type/schemaType")).andExpect(status().isOk());
	}

}
