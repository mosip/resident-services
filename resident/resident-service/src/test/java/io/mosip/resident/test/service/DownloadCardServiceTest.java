package io.mosip.resident.test.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.impl.DownloadCardServiceImpl;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @Mock
    private IdentityServiceImpl identityService;

    @Mock
    private Utilitiy utilitiy;

    private MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO;

    private String result;

    byte[] pdfbytes;

    private ResponseWrapper<DigitalCardStatusResponseDto> responseDto;
    DigitalCardStatusResponseDto digitalCardStatusResponseDto;

    private MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO;

    private Map identityMap;

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
        Mockito.when(utilitiy.signPdf(Mockito.any(), Mockito.any())).thenReturn(pdfbytes);
        Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(utilities.getRidByIndividualId(Mockito.anyString())).thenReturn("1234567890");
        Mockito.when(residentService.getUINCard(Mockito.anyString())).thenReturn(pdfbytes);
        Mockito.when(templateUtil.getIndividualIdType(Mockito.anyString())).thenReturn("UIN");
        Mockito.when(identityService.getIndividualIdForAid(Mockito.anyString())).thenReturn("7841261580");

        downloadPersonalizedCardMainRequestDTO=
                new MainRequestDTO<>();
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        downloadPersonalizedCardDto.setHtml("PGh0bWw+PGhlYWQ+PC9oZWFkPjxib2R5Pjx0YWJsZT48dHI+PHRkPk5hbWU8L3RkPjx0ZD5GUjwvdGQ+PC90cj48dHI+PHRkPkRPQjwvdGQ+PHRkPjE5OTIvMDQvMTU8L3RkPjwvdHI+PHRyPjx0ZD5QaG9uZSBOdW1iZXI8L3RkPjx0ZD45ODc2NTQzMjEwPC90ZD48L3RyPjwvdGFibGU+PC9ib2R5PjwvaHRtbD4=");
        downloadPersonalizedCardMainRequestDTO.setRequest(downloadPersonalizedCardDto);
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(false));
        Mockito.when(environment.getProperty(ResidentConstants.RESIDENT_IDENTITY_SCHEMATYPE)).thenReturn("personalized-card");
        Mockito.when(environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE)).thenReturn("dateOfBirth");
        Mockito.when(identityService.getResidentIndvidualId()).thenReturn("1234567890");
        Map<String, Object> attribute = new HashMap<String, Object>();
        //attribute.put("year", "2022");
        identityMap = new LinkedHashMap();
        identityMap.put("UIN", "8251649601");
        identityMap.put("email", "manojvsp12@gmail.com");
        identityMap.put("phone", "9395910872");
        identityMap.put("dateOfBirth", "1970");
        Mockito.when(identityService.getIdentityAttributes(Mockito.anyString(),Mockito.anyString())).thenReturn(identityMap);
    }

    @Test
    public void testDownloadCardServiceTest()  {
        byte[] actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

    @Test
    public void testGetDownloadCardPdfVID(){
        Mockito.when(templateUtil.getIndividualIdType(Mockito.anyString())).thenReturn("VID");
        byte[] actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

    @Test
    public void testGetDownloadCardPdfAID(){
        Mockito.when(templateUtil.getIndividualIdType(Mockito.anyString())).thenReturn("AID");
        byte[] actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfResidentServiceExceptionTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(identityService.getIndividualIdForAid(Mockito.anyString())).thenThrow(
                new ResidentServiceCheckedException());
        Mockito.when(templateUtil.getIndividualIdType(Mockito.anyString())).thenReturn("AID");
        byte[] actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfApisResourceAccessExceptionTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(identityService.getIndividualIdForAid(Mockito.anyString())).thenThrow(
                new ApisResourceAccessException());
        Mockito.when(templateUtil.getIndividualIdType(Mockito.anyString())).thenReturn("AID");
        byte[] actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfOtpValidationFailedTest() throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
        Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        byte[] actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

    @Test
    public void testDownloadPersonalizedCardSuccess() {
        byte[] actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

    @Test
    public void testDownloadPersonalizedCardPassword(){
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(true));
        byte[] actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult);
    }

}