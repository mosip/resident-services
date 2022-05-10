package io.mosip.resident.test.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.resident.controller.ResidentCredentialController;
import io.mosip.resident.controller.ResidentOtpController;
import io.mosip.resident.dto.*;
import io.mosip.resident.handler.service.ResidentUpdateService;
import io.mosip.resident.handler.service.UinCardRePrintService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.IdAuthServiceImpl;
import io.mosip.resident.test.ResidentTestBootApplication;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResidentTestBootApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class ResidentOtpControllerTest {

    @MockBean
    private ResidentOtpService residentOtpService;

    @MockBean
    private RequestValidator validator;

    @MockBean
    private ResidentUpdateService residentUpdateService;

    @MockBean
    private IdAuthService idAuthService;
	
	@MockBean
	private ResidentVidService vidService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UinCardRePrintService rePrintService;

    @MockBean
    private Utilitiy utilitiy;

    @MockBean
    private Utilities utilities;

    @MockBean
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate residentRestTemplate;

    @Mock
    private AuditUtil audit;

    @InjectMocks
    ResidentOtpController residentOtpController;

    @Autowired
    private MockMvc mockMvc;

    Gson gson = new GsonBuilder().serializeNulls().create();

    String reqJson;

    OtpResponseDTO otpResponseDTO;

    @Before
    public void setup() throws Exception {
        otpResponseDTO = new OtpResponseDTO();
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(residentOtpController).build();
        OtpRequestDTO otpRequestDTO = new OtpRequestDTO();
        otpRequestDTO.setIndividualId("123456");
        otpRequestDTO.setTransactionID("1234327890");
        reqJson = gson.toJson(otpRequestDTO);
    }

    @Test
    public void testCreateRequestGenerationSuccess() throws Exception {

        Mockito.when(residentOtpService.generateOtp(Mockito.any())).thenReturn(otpResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/req/otp").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(reqJson)).andExpect(status().isOk());

    }

}
