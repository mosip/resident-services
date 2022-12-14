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
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.resident.controller.DownloadCardController;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilitiy;
import io.mosip.resident.validator.RequestValidator;
import reactor.util.function.Tuples;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to test download card api.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class DownloadCardControllerTest {
	
    @MockBean
    private RequestValidator validator;

    @Mock
    private AuditUtil audit;
    
    @Mock
    private Environment environment;
    
    @Mock
    private Utilitiy utilitiy;
	
	@MockBean
	private ObjectStoreHelper objectStore;


    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @InjectMocks
    DownloadCardController downloadCardController;

    @MockBean
    DownloadCardService downloadCardService;

    @MockBean
    IdentityServiceImpl identityService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResidentVidService vidService;

    @MockBean
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

    Gson gson = new GsonBuilder().serializeNulls().create();

    String reqJson;

    byte[] pdfbytes;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(downloadCardController).build();
        MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO = new MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setIndividualId("7841261580");
        downloadCardRequestDTO.setOtp("111111");
        downloadCardRequestDTO.setTransactionId("1234567890");
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        downloadCardRequestDTOMainRequestDTO.setId("mosip.resident.download.uin.card");
        reqJson = gson.toJson(downloadCardRequestDTOMainRequestDTO);
        pdfbytes = "uin".getBytes();
        Mockito.when(utilitiy.getFileName(Mockito.anyString(), Mockito.anyString())).thenReturn("file");
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("property");
    }

    @Test
    public void testGetCardSuccess() throws Exception {
        Mockito.when(downloadCardService.getDownloadCardPDF(Mockito.any())).thenReturn(Tuples.of(pdfbytes, "12345"));
        mockMvc.perform(MockMvcRequestBuilders.post("/download-card").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(reqJson.getBytes())).andExpect(status().isOk());
    }

    @Test
    public void testDownloadPersonalizedCard() throws Exception {
        Mockito.when(downloadCardService.downloadPersonalizedCard(Mockito.any())).thenReturn(Tuples.of(pdfbytes, "12345"));
        MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO =
                new MainRequestDTO<>();
        DownloadPersonalizedCardDto downloadPersonalizedCardDto =
                new DownloadPersonalizedCardDto();
        downloadPersonalizedCardDto.setHtml("PGh0bWw+PGhlYWQ+PC9oZWFkPjxib2R5Pjx0YWJsZT48dHI+PHRkPk5hbWU8L3RkPjx0ZD5GUjwvdGQ+PC90cj48dHI+PHRkPkRPQjwvdGQ+PHRkPjE5OTIvMDQvMTU8L3RkPjwvdHI+PHRyPjx0ZD5QaG9uZSBOdW1iZXI8L3RkPjx0ZD45ODc2NTQzMjEwPC90ZD48L3RyPjwvdGFibGU+PC9ib2R5PjwvaHRtbD4=");
        downloadPersonalizedCardMainRequestDTO.setRequest(downloadPersonalizedCardDto);
        reqJson = gson.toJson(downloadPersonalizedCardMainRequestDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/download/personalized-card").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(reqJson.getBytes())).andExpect(status().isOk());
    }

    @Test
    public void testRequestVidCard() throws Exception {
        io.mosip.resident.dto.ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setEventId("123");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
        Mockito.when(downloadCardService.getVidCardEventId(Mockito.any())).thenReturn(vidDownloadCardResponseDtoResponseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/request-card/vid/9086273859467431")).andExpect(status().isOk());
    }

}
