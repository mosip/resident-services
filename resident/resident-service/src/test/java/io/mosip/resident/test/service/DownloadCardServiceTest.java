package io.mosip.resident.test.service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.DownloadCardServiceImpl;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
import reactor.util.function.Tuple2;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

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

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private ResidentVidService vidService;

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
        Mockito.when(identityService.getIndividualIdType(Mockito.anyString())).thenReturn("UIN");
        Mockito.when(identityService.getIndividualIdForAid(Mockito.anyString())).thenReturn("7841261580");
        Mockito.when(utilitiy.createEntity()).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utilitiy.createEventId()).thenReturn("123");

        ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId("12345");
        Mockito.when(residentTransactionRepository.findByAid(Mockito.anyString())).thenReturn(residentTransactionEntity);

        downloadPersonalizedCardMainRequestDTO=
                new MainRequestDTO<>();
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        downloadPersonalizedCardDto.setHtml("PGh0bWw+PGhlYWQ+PC9oZWFkPjxib2R5Pjx0YWJsZT48dHI+PHRkPk5hbWU8L3RkPjx0ZD5GUjwvdGQ+PC90cj48dHI+PHRkPkRPQjwvdGQ+PHRkPjE5OTIvMDQvMTU8L3RkPjwvdHI+PHRyPjx0ZD5QaG9uZSBOdW1iZXI8L3RkPjx0ZD45ODc2NTQzMjEwPC90ZD48L3RyPjwvdGFibGU+PC9ib2R5PjwvaHRtbD4=");
        downloadPersonalizedCardMainRequestDTO.setRequest(downloadPersonalizedCardDto);
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(false));
        Mockito.when(environment.getProperty(ResidentConstants.RESIDENT_IDENTITY_SCHEMATYPE)).thenReturn("personalized-card");
        Mockito.when(environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE)).thenReturn("dateOfBirth");
        Mockito.when(environment.getProperty(ResidentConstants.MOSIP_CREDENTIAL_TYPE_PROPERTY)).thenReturn("credentialType");
        Mockito.when(environment.getProperty(ResidentConstants.CREDENTIAL_ISSUER)).thenReturn("credentialType");
        Mockito.when(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_FLAG)).thenReturn("true");
        Mockito.when(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_KEY)).thenReturn("true");
        Mockito.when(identityService.getResidentIndvidualId()).thenReturn("1234567890");
        Mockito.when(identityService.getResidentIndvidualId()).thenReturn("1234567890");
        identityMap = new LinkedHashMap();
        identityMap.put("UIN", "8251649601");
        identityMap.put("email", "manojvsp12@gmail.com");
        identityMap.put("phone", "9395910872");
        identityMap.put("dateOfBirth", "1970");
        Mockito.when(identityService.getIdentityAttributes(Mockito.anyString(),Mockito.anyString())).thenReturn(identityMap);
    }

    @Test
    public void testDownloadCardServiceTest()  {
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testGetDownloadCardPdfVID(){
        Mockito.when(identityService.getIndividualIdType(Mockito.anyString())).thenReturn("VID");
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testGetDownloadCardPdfAID(){
        Mockito.when(identityService.getIndividualIdType(Mockito.anyString())).thenReturn("AID");
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfResidentServiceExceptionTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(identityService.getIndividualIdForAid(Mockito.anyString())).thenThrow(
                new ResidentServiceCheckedException());
        Mockito.when(identityService.getIndividualIdType(Mockito.anyString())).thenReturn("AID");
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfApisResourceAccessExceptionTest() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Mockito.when(identityService.getIndividualIdForAid(Mockito.anyString())).thenThrow(
                new ApisResourceAccessException());
        Mockito.when(identityService.getIndividualIdType(Mockito.anyString())).thenReturn("AID");
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfOtpValidationFailedTest() throws ResidentServiceCheckedException, ApisResourceAccessException, OtpValidationFailedException {
        Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfApiResourceException() throws OtpValidationFailedException, ApisResourceAccessException {
        Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(utilities.getRidByIndividualId(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfOtpValidationException() throws OtpValidationFailedException, ApisResourceAccessException {
        Mockito.when(idAuthService.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new OtpValidationFailedException());
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testDownloadPersonalizedCardSuccess() {
    	Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testDownloadPersonalizedCardSuccessWithListAttributes() throws ResidentServiceCheckedException, IOException {
        Mockito.when(environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE)).thenReturn("firstName");
        Map<String, Object> name = new HashMap<>();
        name.put("language", "eng");
        name.put("value", "kamesh");
        identityMap.put("firstName", List.of(name));
        Mockito.when(utilities.getLanguageCode()).thenReturn("eng");
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testDownloadPersonalizedCardPassword(){
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(true));
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testDownloadPersonalizedCardPasswordFailed(){
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(true));
        Mockito.when(utilitiy.getPassword(Mockito.anyList())).thenThrow(
                new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD));
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testDownloadPersonalizedCardResidentServiceCheckedException() throws ResidentServiceCheckedException, IOException {
        Mockito.when(identityService.getIdentityAttributes(Mockito.anyString(), Mockito.anyString())).thenThrow(
                new ResidentServiceCheckedException());
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testDownloadPersonalizedCardIOException() throws ResidentServiceCheckedException, IOException {
        Mockito.when(identityService.getIdentityAttributes(Mockito.anyString(), Mockito.anyString())).thenThrow(
                new IOException());
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testGetVidCardEventId() throws BaseCheckedException {
        io.mosip.resident.dto.ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setEventId("123");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
        io.mosip.resident.dto.ResponseWrapper<ResidentCredentialResponseDto> responseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setId("123");
        residentCredentialResponseDto.setRequestId("123");
        responseWrapper.setResponse(residentCredentialResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        Mockito.when(utilitiy.createEntity()).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utilitiy.createEventId()).thenReturn("123");
        assertEquals(vidDownloadCardResponseDtoResponseWrapper.getResponse().getEventId(),
                downloadCardService.getVidCardEventId("123").getResponse().getEventId());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetVidCardEventIdFailed() throws BaseCheckedException {
        io.mosip.resident.dto.ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setEventId("123");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
        io.mosip.resident.dto.ResponseWrapper<ResidentCredentialResponseDto> responseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setId("123");
        residentCredentialResponseDto.setRequestId("123");
        responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.VID_REQUEST_CARD_FAILED.getErrorCode(),
                ResidentErrorCode.VID_REQUEST_CARD_FAILED.getErrorMessage())));
        responseWrapper.setResponse(residentCredentialResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        assertEquals(vidDownloadCardResponseDtoResponseWrapper.getResponse().getEventId(),
                downloadCardService.getVidCardEventId("123").getResponse().getEventId());
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetVidCardEventIdApisResourceAccessException() throws BaseCheckedException {
        io.mosip.resident.dto.ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setEventId("123");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());
        assertEquals(vidDownloadCardResponseDtoResponseWrapper.getResponse().getEventId(),
                downloadCardService.getVidCardEventId("123").getResponse().getEventId());
    }

    @Test(expected = BaseCheckedException.class)
    public void testGetVidCardEventIdResidentServiceCheckedException() throws BaseCheckedException, IOException {
        io.mosip.resident.dto.ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setEventId("123");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
        io.mosip.resident.dto.ResponseWrapper<ResidentCredentialResponseDto> responseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setId("123");
        residentCredentialResponseDto.setRequestId("123");
        responseWrapper.setResponse(residentCredentialResponseDto);
        Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenThrow(new IOException());
        assertEquals(vidDownloadCardResponseDtoResponseWrapper.getResponse().getEventId(),
                downloadCardService.getVidCardEventId("123").getResponse().getEventId());
    }

    @Test
    public void testGetVidCardEventIdWithVidDetails() throws BaseCheckedException, IOException {
        io.mosip.resident.dto.ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setEventId("123");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
        io.mosip.resident.dto.ResponseWrapper<ResidentCredentialResponseDto> responseWrapper =
                new io.mosip.resident.dto.ResponseWrapper<>();
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setId("123");
        residentCredentialResponseDto.setRequestId("123");
        responseWrapper.setResponse(residentCredentialResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        io.mosip.resident.dto.ResponseWrapper<List<Map<String,?>>> vidResponse = new io.mosip.resident.dto.ResponseWrapper<>();
        Map<String, Object> vidDetails = new HashMap<>();
        vidDetails.put("vidType", "perpetual");
        List<Map<String, ?>> vidList = new ArrayList<>();
        vidDetails.put("vid", "123");
        vidDetails.put("maskedVid", "1******4");
        vidDetails.put("expiryTimeStamp", "1234343434");
        vidDetails.put("expiryTimeStamp", "1234343434");
        vidDetails.put("genratedOnTimestamp", "1234343434");
        vidDetails.put("transactionLimit", "1234343434");
        vidDetails.put("transactionCount", "1234343434");
        vidList.add(vidDetails);
        vidResponse.setResponse(vidList);
        Mockito.when(vidService.retrieveVids(Mockito.anyString())).thenReturn(vidResponse);
        Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenReturn("3425636374");
        Mockito.when(utilitiy.createEntity()).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utilitiy.createEventId()).thenReturn("123");
        assertEquals(vidDownloadCardResponseDtoResponseWrapper.getResponse().getEventId(),
                downloadCardService.getVidCardEventId("123").getResponse().getEventId());
    }

}