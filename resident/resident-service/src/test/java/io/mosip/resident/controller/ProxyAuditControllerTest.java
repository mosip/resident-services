package io.mosip.resident.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.dto.AuthenticatedAuditRequestDto;
import io.mosip.resident.dto.UnauthenticatedAuditRequestDto;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utility;
import org.springframework.web.context.WebApplicationContext;
import reactor.util.function.Tuples;

/**
 * Resident proxy audit controller test class.
 *
 * @author Ritik Jain
 */
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@Import(EnvUtil.class)
@ActiveProfiles("test")
public class ProxyAuditControllerTest {

	@MockBean
	private ProxyIdRepoService proxyIdRepoService;

	@InjectMocks
	private ProxyAuditController proxyAuditController;

	@Mock
	private AuditUtil auditUtil;

	@Mock
	private Utility utility;

	@MockBean
	private ResidentServiceImpl residentService;

	@MockBean
	private Utility utilityBean;

	@MockBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@MockBean
	private AuthTransactionCallbackController authTransactionCallbackController;

	@MockBean
	private DocumentController documentController;

	@MockBean
	private ObjectStoreHelper objectStore;

	@MockBean
	private IdAuthController idAuthController;

	@MockBean
	private ResidentVidService vidService;

	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

	private AuthenticatedAuditRequestDto authenticatedAuditRequestDto;

	private UnauthenticatedAuditRequestDto unauthenticatedAuditRequestDto;

	@Autowired
	private MockMvc mockMvc;

	Gson gson = new GsonBuilder().serializeNulls().create();

	String reqJson;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(proxyAuditController).build();
		authenticatedAuditRequestDto = new AuthenticatedAuditRequestDto();
		authenticatedAuditRequestDto.setAuditEventId("RES-SER-1111");
		authenticatedAuditRequestDto.setModuleId("RES-SER");
		authenticatedAuditRequestDto.setModuleName("Residence service");
		unauthenticatedAuditRequestDto = new UnauthenticatedAuditRequestDto();
		unauthenticatedAuditRequestDto.setAuditEventId("RES-SER-1111");
		unauthenticatedAuditRequestDto.setModuleId("RES-SER");
		unauthenticatedAuditRequestDto.setModuleName("Residence service");
	}

	@Test
	public void testAuthAuditLog() throws Exception {
		Mockito.when(auditUtil.getRefIdHashAndType()).thenReturn(Tuples.of("23455683456", IdType.AID.name()));
		reqJson = gson.toJson(authenticatedAuditRequestDto);
		mockMvc.perform(MockMvcRequestBuilders.post("/auth-proxy/audit/log").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(reqJson.getBytes())).andExpect(status().isOk());
	}

	@Test
	public void testAuditLogWithId() throws Exception {
		Mockito.when(auditUtil.getRefIdHashAndTypeFromIndividualId(Mockito.anyString())).thenReturn(Tuples.of("23455683456", IdType.AID.name()));
		unauthenticatedAuditRequestDto.setId("23456");
		reqJson = gson.toJson(unauthenticatedAuditRequestDto);
		mockMvc.perform(MockMvcRequestBuilders.post("/proxy/audit/log").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(reqJson.getBytes())).andExpect(status().isOk());
	}

	@Test
	public void testAuditLogWithNullId() throws Exception {
		reqJson = gson.toJson(unauthenticatedAuditRequestDto);
		mockMvc.perform(MockMvcRequestBuilders.post("/proxy/audit/log").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(reqJson.getBytes())).andExpect(status().isOk());
	}

}