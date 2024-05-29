package io.mosip.resident.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.mosip.idrepository.core.util.EnvUtil;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.web.context.WebApplicationContext;

/**
 * Resident identity controller test class.
 *
 * @author Ritik Jain
 */
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(EnvUtil.class)
@ActiveProfiles("test")
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

	@Mock
	private RequestValidator validator;

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

	@MockBean
	private Utility utilityBean;

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
		ReflectionTestUtils.setField(identityController, "residentIdentityInfoId", "identity.id");
	}

	@Test
	public void testGetInputAttributeValues() throws Exception {
		Mockito.when(idServiceImpl.getIdentityAttributes(Mockito.anyString(),Mockito.anyString())).thenReturn(identityMap);
		mockMvc.perform(MockMvcRequestBuilders.get("/identity/info/type/schemaType")).andExpect(status().isOk());
	}

	@Test(expected = ResidentServiceException.class)
	public void testGetInputAttributeValuesWithInvalidInputException()
			throws ApisResourceAccessException, ResidentServiceCheckedException, IOException {
		Mockito.doThrow(new InvalidInputException()).when(validator).validateSchemaType(Mockito.anyString());
		identityController.getInputAttributeValues("schema-type");
	}
}