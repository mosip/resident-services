package io.mosip.resident.controller;

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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuples;

/**
 * Resident proxy audit controller test class.
 * 
 * @author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
public class ProxyAuditControllerTest {
	
    @Mock
    private ProxyIdRepoService proxyIdRepoService;

	@InjectMocks
	private ProxyAuditController proxyAuditController;

	@Mock
	private AuditUtil auditUtil;
	
	@Mock
	private Utility utility;
	
	@Mock
    private ResidentServiceImpl residentService;

	@Mock
	private Utility utilityBean;

	@Mock
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@Mock
	private AuthTransactionCallbackController authTransactionCallbackController;

	@Mock
	private DocumentController documentController;

	@Mock
	private ObjectStoreHelper objectStore;

	@Mock
	private IdAuthController idAuthController;

	@Mock
	private ResidentVidService vidService;

	@Mock
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
