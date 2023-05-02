package io.mosip.resident.test.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.resident.controller.ResidentCredentialController;
import io.mosip.resident.dto.CredentialCancelRequestResponseDto;
import io.mosip.resident.dto.CredentialRequestStatusResponseDto;
import io.mosip.resident.dto.PartnerCredentialTypePolicyDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResidentCredentialResponseDtoV2;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.dto.ShareCredentialRequestDto;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.ResidentConfigServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.validator.RequestValidator;
import reactor.util.function.Tuples;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ResidentCredentialControllerTest {
	
    @MockBean
    private ProxyIdRepoService proxyIdRepoService;

    @MockBean
    private ResidentCredentialService residentCredentialService;

    @Mock
    CbeffImpl cbeff;

    @MockBean
    private RequestValidator validator;

    @Mock
    private AuditUtil audit;
	
	@MockBean
	private ResidentVidService vidService;
	
	@MockBean
	private DocumentService docService;

    @MockBean
    private ResidentServiceImpl residentService;
    
    @MockBean
	private ResidentConfigServiceImpl residentConfigService;
	
	@MockBean
	private ObjectStoreHelper objectStore;

    @MockBean
    private TemplateUtil templateUtil;

    @MockBean
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

    @MockBean
    @Qualifier("selfTokenRestTemplate")
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

    PartnerCredentialTypePolicyDto partnerCredentialTypeReqResponse;
    
    ResidentCredentialResponseDtoV2 dtoV2;

    String reqCredentialEventJson;

    byte[] pdfbytes;

    @Before
    public void setup() throws Exception {
        credentialReqStatusResponse = new CredentialRequestStatusResponseDto();
        credentialCancelReqResponse = new CredentialCancelRequestResponseDto();
        credentialReqResponse = new ResidentCredentialResponseDto();
        partnerCredentialTypeReqResponse = new PartnerCredentialTypePolicyDto();
        dtoV2 = new ResidentCredentialResponseDtoV2();
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(residentCredentialController).build();
        ResidentCredentialRequestDto credentialRequestDto = new ResidentCredentialRequestDto();
        credentialRequestDto.setIndividualId("123456");
        RequestWrapper<ResidentCredentialRequestDto> requestDTO = new RequestWrapper<>();
        requestDTO.setRequest(credentialRequestDto);
        reqJson = gson.toJson(requestDTO);
        pdfbytes = "uin".getBytes();
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
    public void testPartnerIdCredentialType() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/req/policy/partnerId/1/credentialType/credentialType").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void testRequestShareCredWithPartner() throws Exception {
		Mockito.when(residentCredentialService.shareCredential(Mockito.any(), Mockito.anyString()))
				.thenReturn(Tuples.of(dtoV2, "12345"));
        ShareCredentialRequestDto request = new ShareCredentialRequestDto();
        SharableAttributesDTO attr = new SharableAttributesDTO();
        attr.setAttributeName("name");
        attr.setMasked(false);
		request.setSharableAttributes(List.of(attr));
		request.setPurpose("banking");
		RequestWrapper<ShareCredentialRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(request);
        mockMvc.perform(MockMvcRequestBuilders.post("/share-credential").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(gson.toJson(requestWrapper).getBytes())).andExpect(status().isOk());
    }
    
}
