package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.impl.DownloadCardServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * This class is used to create service class test  for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class DownloadCardServiceTest {

    @InjectMocks
    private DownloadCardService downloadCardService = new DownloadCardServiceImpl();

    @Mock
    private IdAuthService idAuthService;

    @Mock
    private Utilities utilities;

    @Mock
    private Environment environment;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    @Mock
    private AuditUtil auditUtil;

    @Mock
    private ResidentServiceImpl residentService;

    @Mock
    private TemplateUtil templateUtil;

    private MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO;

    private String result;

    byte[] pdfbytes;

    private ResponseWrapper<DigitalCardStatusResponseDto> responseDto;
    DigitalCardStatusResponseDto digitalCardStatusResponseDto;

    @Before
    public void setup() throws Exception {
        downloadCardRequestDTOMainRequestDTO = new MainRequestDTO<>();
        DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
        downloadCardRequestDTO.setTransactionId("1234567890");
        downloadCardRequestDTO.setOtp("111111");
        downloadCardRequestDTO.setIndividualId("7841261580");
        downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
        result = "result";
        pdfbytes = result.getBytes();
        Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(utilities.getRidByIndividualId(Mockito.anyString())).thenReturn("1234567890");
        Mockito.when(residentService.getUINCard(Mockito.anyString())).thenReturn(pdfbytes);
        Mockito.when(templateUtil.getIndividualIdType(Mockito.anyString())).thenReturn("UIN");
    }

    @Test
    public void testAcknowledgementServiceTest()  {
        byte[] actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }
}