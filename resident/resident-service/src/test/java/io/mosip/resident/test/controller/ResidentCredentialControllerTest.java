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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.resident.controller.ResidentCredentialController;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.RIDDigitalCardRequestDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ResidentCredentialControllerTest {

	@MockBean
	private ResidentCredentialService residentCredentialService;

	@Mock
	CbeffImpl cbeff;

	@MockBean
	private RequestValidator validator;

	@Mock
	private AuditUtil audit;

	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

	@MockBean
	private RestTemplate residentRestTemplate;

	@InjectMocks
	ResidentCredentialController residentCredentialController;

	@Autowired
	private MockMvc mockMvc;

	Gson gson = new GsonBuilder().serializeNulls().create();

	String reqJson;

	ResidentCredentialResponseDto credentialReqResponse;

	CredentialCancelRequestResponseDto credentialCancelReqResponse;

	CredentialRequestStatusResponseDto credentialReqStatusResponse;

	String reqCredentialEventJson;

	byte[] pdfbytes;

	String ridDigitalCardRequestJson;

	@Before
	public void setup() throws Exception {
		credentialReqStatusResponse = new CredentialRequestStatusResponseDto();
		credentialCancelReqResponse = new CredentialCancelRequestResponseDto();
		credentialReqResponse = new ResidentCredentialResponseDto();
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(residentCredentialController).build();
		ResidentCredentialRequestDto credentialRequestDto = new ResidentCredentialRequestDto();
		credentialRequestDto.setIndividualId("123456");
		reqJson = gson.toJson(credentialRequestDto);
		pdfbytes = "uin".getBytes();
		RIDDigitalCardRequestDto ridDigitalCardRequestDto = new RIDDigitalCardRequestDto();
		ridDigitalCardRequestDto.setIndividualId("10001090900001020220414054750");
		ridDigitalCardRequestDto.setOtp("123456");
		ridDigitalCardRequestDto.setTransactionID("123456789");
		RequestWrapper<RIDDigitalCardRequestDto> ridDigitalCardRequestDtoWrapper = new RequestWrapper<>();
		ridDigitalCardRequestDtoWrapper.setRequest(ridDigitalCardRequestDto);
		ridDigitalCardRequestJson = gson.toJson(ridDigitalCardRequestDtoWrapper);
	}

	@Test
	public void testCreateRequestGenerationSuccess() throws Exception {

		Mockito.when(residentCredentialService.reqCredential(Mockito.any())).thenReturn(credentialReqResponse);

		mockMvc.perform(MockMvcRequestBuilders.post("/req/credential").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(reqJson.getBytes())).andExpect(status().isOk());

	}

	@Test
	public void testCancelRequestSuccess() throws Exception {

		Mockito.when(residentCredentialService.cancelCredentialRequest(Mockito.any()))
				.thenReturn(credentialCancelReqResponse);

		mockMvc.perform(MockMvcRequestBuilders.get("/req/credential/cancel/requestId")
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());

	}

	@Test
	public void testgetCredentialRequestStatusSuccess() throws Exception {

		Mockito.when(residentCredentialService.getStatus(Mockito.any())).thenReturn(credentialReqStatusResponse);

		mockMvc.perform(MockMvcRequestBuilders.get("/req/credential/status/requestId")
				.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());

	}

	@Test
	public void testgGetCardSuccess() throws Exception {

		Mockito.when(residentCredentialService.getCard(Mockito.any())).thenReturn(pdfbytes);

		mockMvc.perform(MockMvcRequestBuilders.get("/req/card/requestId")
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

	@Test
	public void testGetCredentialTypesSuccess() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/credential/types").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());

	}

	@Test
	public void testGetRIDDigitalCardSuccess() throws Exception {

		Mockito.when(residentCredentialService.getRIDDigitalCard(Mockito.any())).thenReturn(pdfbytes);

		mockMvc.perform(MockMvcRequestBuilders.post("/req/rid-digital-card")
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(ridDigitalCardRequestJson.getBytes()))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.content()
				.contentType(MediaType.APPLICATION_PDF_VALUE));

	}

}
