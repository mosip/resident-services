package io.mosip.resident.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.OrderCardService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Resident order card controller test class.
 * 
 * @author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
public class OrderCardControllerTest {
	
    @Mock
    private ProxyIdRepoService proxyIdRepoService;

	@InjectMocks
	private OrderCardController orderCardController;

	@Mock
	private OrderCardService orderCardService;

	@Mock
	private ResidentVidService vidService;

	@Mock
	private AuditUtil auditUtil;

	@Mock
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@Mock
	private AuthTransactionCallbackController authTransactionCallbackController;

	@Mock
	private DocumentController documentController;

	@Mock
	private IdAuthController idAuthController;

	@Mock
	private IdentityController identityController;

	@Mock
	private ObjectStoreHelper objectStore;

	@Mock
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;
	
	@Mock
    private ResidentServiceImpl residentService;

	@Autowired
	private MockMvc mockMvc;

	private ResponseWrapper responseWrapper;
	
	@Mock
	private IdentityServiceImpl identityServiceImpl;

	Gson gson = new GsonBuilder().serializeNulls().create();

	String reqJson;

	@Before
	public void setUp() throws Exception {
		responseWrapper = new ResponseWrapper<>();
		responseWrapper.setVersion("v1");
		responseWrapper.setId("1");
		RequestWrapper<ResidentCredentialRequestDto> requestWrapper = new RequestWrapper();
		ResidentCredentialRequestDto residentCredentialRequestDto = new ResidentCredentialRequestDto();
		residentCredentialRequestDto.setTransactionID("1234567890");
		residentCredentialRequestDto.setIndividualId("8251649601");
		requestWrapper.setRequest(residentCredentialRequestDto);
		reqJson = gson.toJson(requestWrapper);
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(orderCardController).build();
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any());
		Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("1234Id");
	}

	@Test
	public void testSendPhysicalCard() throws Exception {
		Mockito.when(orderCardService.sendPhysicalCard(Mockito.any()))
				.thenReturn((ResidentCredentialResponseDto) responseWrapper.getResponse());
		mockMvc.perform(MockMvcRequestBuilders.post("/sendCard").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(reqJson.getBytes())).andExpect(status().isOk());
	}
	
	@Test
	public void testPhysicalCardOrder() throws Exception {
		Mockito.when(orderCardService.getRedirectUrl(Mockito.any(),Mockito.any())).thenReturn("URL");
		mockMvc.perform(MockMvcRequestBuilders.get("/physical-card/order?partnerId=mosip_partnerorg1667786709933&redirectUri=vdsvdvds")).andExpect(status().isFound());
	}
	
	@Test
	public void testPhysicalCardOrderRedirect() throws Exception {
		Mockito.when(orderCardService.physicalCardOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("URL");
		mockMvc.perform(MockMvcRequestBuilders.get(
				"/physical-card/order-redirect?redirectUrl=aHR0cHM6Ly93d3cubWFkZWludGV4dC5jb20v&paymentTransactionId=12345dsvdvds&eventId=123456&residentFullAddress=fgfhfghgf")).andExpect(status().isFound());
	}

}