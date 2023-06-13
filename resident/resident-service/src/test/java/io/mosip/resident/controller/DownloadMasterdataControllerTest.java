package io.mosip.resident.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DownLoadMasterDataService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to test download master data controller api.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class DownloadMasterdataControllerTest {
	
    @MockBean
    private RequestValidator validator;

    @Mock
    private AuditUtil audit;
	
	@MockBean
	private ObjectStoreHelper objectStore;


    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @InjectMocks
    DownLoadMasterDataController downLoadMasterDataController;

    @MockBean
    DownLoadMasterDataService downLoadMasterDataService;

    @MockBean
    IdentityServiceImpl identityService;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResidentVidService vidService;

    @MockBean
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> encryptor;

    @MockBean
    private AuditUtil auditUtil;
    
    @MockBean
    private ResidentServiceImpl residentService;

    @Mock
    private Utility utility;

    @Mock
    private Environment environment;

    Gson gson = new GsonBuilder().serializeNulls().create();

    String reqJson;

    byte[] pdfbytes;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(downLoadMasterDataController).build();
        MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO = new MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setIndividualId("7841261580");
        downloadCardRequestDTO.setOtp("111111");
        downloadCardRequestDTO.setTransactionId("1234567890");
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        downloadCardRequestDTOMainRequestDTO.setId("mosip.resident.download.uin.card");
        reqJson = gson.toJson(downloadCardRequestDTOMainRequestDTO);
        pdfbytes = "uin".getBytes();
        Mockito.when(utility.getFileName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn("fileName");
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("property");
    }

    @Test
    public void testDownloadRegistrationCentersByHierarchyLevel() throws Exception {
        Mockito.when(downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(Mockito.any(),
                Mockito.any(), Mockito.any())).thenReturn( new ByteArrayInputStream(pdfbytes));
        mockMvc.perform(MockMvcRequestBuilders.get("/download/registration-centers-list?langcode=eng&hierarchylevel=5&name=14022")).
               andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testDownloadRegistrationCentersByHierarchyLevelInvalidInputException() throws Exception {
        doThrow(new InvalidInputException()).
                when(validator).validateOnlyLanguageCode(any());
        Mockito.when(downLoadMasterDataService.downloadRegistrationCentersByHierarchyLevel(Mockito.any(),
                Mockito.any(), Mockito.any())).thenReturn( new ByteArrayInputStream(pdfbytes));
        mockMvc.perform(MockMvcRequestBuilders.get("/download/registration-centers-list?langcode=eng&hierarchylevel=5&name=14022")).
                andExpect(status().isOk());
    }

    @Test
    public void testDownloadNearestRegistrationCenters() throws Exception {
        Mockito.when(downLoadMasterDataService.getNearestRegistrationcenters(Mockito.anyString(),
                Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyInt())).thenReturn( new ByteArrayInputStream(pdfbytes));
        mockMvc.perform(MockMvcRequestBuilders.get
                        ("/download/nearestRegistrationcenters?langcode=eng&longitude=1&latitude=1&proximitydistance=1")).
                andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testDownloadNearestRegistrationCentersFailed() throws Exception {
        doThrow(new InvalidInputException()).
                when(validator).validateOnlyLanguageCode(any());
        Mockito.when(downLoadMasterDataService.getNearestRegistrationcenters(Mockito.anyString(),
                Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyInt())).thenReturn( new ByteArrayInputStream(pdfbytes));
        mockMvc.perform(MockMvcRequestBuilders.get
                        ("/download/nearestRegistrationcenters?langcode=eng&longitude=1&latitude=1&proximitydistance=1")).
                andExpect(status().isOk());
    }

    @Test
    public void testDownloadSupportingDocsByLanguage() throws Exception {
        Mockito.when(downLoadMasterDataService.downloadSupportingDocsByLanguage(Mockito.anyString())).
                thenReturn( new ByteArrayInputStream(pdfbytes));
        mockMvc.perform(MockMvcRequestBuilders.get
                        ("/download/supporting-documents?langcode=eng")).
                andExpect(status().isOk());
    }

    @Test(expected = Exception.class)
    public void testDownloadSupportingDocsByLanguageFailed() throws Exception {
        doThrow(new InvalidInputException()).
                when(validator).validateOnlyLanguageCode(any());
        Mockito.when(downLoadMasterDataService.downloadSupportingDocsByLanguage(Mockito.anyString())).
                thenReturn( new ByteArrayInputStream(pdfbytes));
        mockMvc.perform(MockMvcRequestBuilders.get
                        ("/download/supporting-documents?langcode=eng")).
                andExpect(status().isOk());
    }
}
