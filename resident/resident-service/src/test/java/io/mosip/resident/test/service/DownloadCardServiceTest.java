package io.mosip.resident.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.resident.constant.PacketStatus;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TransactionStage;
import io.mosip.resident.dto.CheckStatusResponseDTO;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentCredentialService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.service.impl.DownloadCardServiceImpl;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;

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
    private ResidentCredentialService residentCredentialService;

    @Mock
    private IdentityServiceImpl identityService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Utility utility;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private ResidentVidService vidService;

    private MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO;

    private String result;

    byte[] pdfbytes;

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
        Mockito.when(utility.signPdf(Mockito.any(), Mockito.any())).thenReturn(pdfbytes);
        Mockito.when(idAuthService.validateOtpv2(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(utilities.getRidByIndividualId(Mockito.anyString())).thenReturn("1234567890");
        Mockito.when(residentCredentialService.getCard(Mockito.anyString(), isNull(), isNull())).thenReturn(pdfbytes);
        Mockito.when(identityService.getIndividualIdType(Mockito.anyString())).thenReturn("UIN");
        Mockito.when(identityService.getIndividualIdForAid(Mockito.anyString())).thenReturn("7841261580");
        Mockito.when(utility.createEntity()).thenReturn(new ResidentTransactionEntity());
        Mockito.when(utility.createEventId()).thenReturn("12345");

        ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId("12345");
        downloadPersonalizedCardMainRequestDTO=
                new MainRequestDTO<>();
        DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
        downloadPersonalizedCardDto.setHtml("PGh0bWw+PGhlYWQ+PC9oZWFkPjxib2R5Pjx0YWJsZT48dHI+PHRkPk5hbWU8L3RkPjx0ZD5GUjwvdGQ+PC90cj48dHI+PHRkPkRPQjwvdGQ+PHRkPjE5OTIvMDQvMTU8L3RkPjwvdHI+PHRyPjx0ZD5QaG9uZSBOdW1iZXI8L3RkPjx0ZD45ODc2NTQzMjEwPC90ZD48L3RyPjwvdGFibGU+PC9ib2R5PjwvaHRtbD4=");
        downloadPersonalizedCardMainRequestDTO.setRequest(downloadPersonalizedCardDto);
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(false));
        Mockito.when(environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE)).thenReturn("dateOfBirth");
        Mockito.when(environment.getProperty(ResidentConstants.MOSIP_CREDENTIAL_TYPE_PROPERTY)).thenReturn("credentialType");
        Mockito.when(environment.getProperty(ResidentConstants.CREDENTIAL_ISSUER)).thenReturn("credentialType");
        Mockito.when(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_FLAG)).thenReturn("true");
        Mockito.when(environment.getProperty(ResidentConstants.CREDENTIAL_ENCRYPTION_KEY)).thenReturn("true");
        Mockito.when(identityService.getResidentIndvidualIdFromSession()).thenReturn("1234567890");
        Mockito.when(identityService.getUinForIndividualId(Mockito.anyString())).thenReturn("3425636374");
        Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenReturn("3425636374");
        identityMap = new LinkedHashMap();
        identityMap.put("UIN", "8251649601");
        identityMap.put("email", "manojvsp12@gmail.com");
        identityMap.put("phone", "9395910872");
        identityMap.put("dateOfBirth", "1970");
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
        assertNotNull(actualResult);
    }

    @Test
    public void testGetDownloadCardPdfAID() throws ApisResourceAccessException, IOException {
    	String rid = "7841261580";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(ResidentConstants.AID_STATUS, "SUCCESS");
        hashMap.put(ResidentConstants.TRANSACTION_TYPE_CODE, TransactionStage.CARD_READY_TO_DOWNLOAD.name());
        Mockito.when(utilities.getPacketStatus(rid)).thenReturn(hashMap);
        Mockito.when(identityService.getIndividualIdType(Mockito.anyString())).thenReturn("AID");
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertNotNull(actualResult.getT1());
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
        Mockito.when(idAuthService.validateOtpv2(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfApiResourceException() throws OtpValidationFailedException, ApisResourceAccessException, ResidentServiceCheckedException {
        Mockito.when(idAuthService.validateOtpv2(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(utilities.getRidByIndividualId(Mockito.anyString())).thenThrow(new ApisResourceAccessException());
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testGetDownloadCardPdfOtpValidationException() throws OtpValidationFailedException, ApisResourceAccessException, ResidentServiceCheckedException {
        Mockito.when(idAuthService.validateOtpv2(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new OtpValidationFailedException());
        Tuple2<byte[], String> actualResult = downloadCardService.getDownloadCardPDF(downloadCardRequestDTOMainRequestDTO);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testDownloadPersonalizedCardSuccess() {
    	Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO, 0);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testDownloadPersonalizedCardSuccessWithListAttributes() throws ResidentServiceCheckedException, IOException {
        Mockito.when(environment.getProperty(ResidentConstants.PASSWORD_ATTRIBUTE)).thenReturn("firstName");
        Map<String, Object> name = new HashMap<>();
        name.put("language", "eng");
        name.put("value", "kamesh");
        identityMap.put("firstName", List.of(name));
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO, 0);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testDownloadPersonalizedCardPassword(){
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(true));
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO, 0);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testDownloadPersonalizedCardPasswordFailed(){
        Mockito.when(environment.getProperty(ResidentConstants.IS_PASSWORD_FLAG_ENABLED)).thenReturn(String.valueOf(true));
        Mockito.when(utility.getPassword(Mockito.anyList())).thenThrow(
                new ResidentServiceException(ResidentErrorCode.DOWNLOAD_PERSONALIZED_CARD));
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO, 0);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testDownloadPersonalizedCardResidentServiceCheckedException() throws ResidentServiceCheckedException, IOException {
        Mockito.when(identityService.getIdentityAttributes(Mockito.anyString(), Mockito.isNull())).thenThrow(
                new ResidentServiceCheckedException());
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO, 0);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test(expected = ResidentServiceException.class)
    public void testDownloadPersonalizedCardIOException() throws ResidentServiceCheckedException, IOException {
        Mockito.when(identityService.getIdentityAttributes(Mockito.anyString(), Mockito.isNull())).thenThrow(
                new IOException());
        Tuple2<byte[], String> actualResult = downloadCardService.downloadPersonalizedCard(downloadPersonalizedCardMainRequestDTO, 0);
        assertEquals(pdfbytes, actualResult.getT1());
    }

    @Test
    public void testGetVidCardEventId() throws BaseCheckedException {
		ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper = new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setStatus("success");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
		ResponseWrapper<ResidentCredentialResponseDto> responseWrapper = new ResponseWrapper<>();
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setId("123");
        residentCredentialResponseDto.setRequestId("123");
        responseWrapper.setResponse(residentCredentialResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
		assertEquals("12345", downloadCardService.getVidCardEventId("123", 0).getT2());
    }
    
    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetVidCardEventIdNestedIf() throws BaseCheckedException, IOException {
		Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenReturn("123456789");
		downloadCardService.getVidCardEventId("123", 0);
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetVidCardEventIdFailed() throws BaseCheckedException {
		ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper = new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
		ResponseWrapper<ResidentCredentialResponseDto> responseWrapper = new ResponseWrapper<>();
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setId("123");
        residentCredentialResponseDto.setRequestId("123");
        responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.VID_REQUEST_CARD_FAILED.getErrorCode(),
                ResidentErrorCode.VID_REQUEST_CARD_FAILED.getErrorMessage())));
        responseWrapper.setResponse(residentCredentialResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
		downloadCardService.getVidCardEventId("123", 0);
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetVidCardEventIdApisResourceAccessException() throws BaseCheckedException {
		ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper = new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());
		downloadCardService.getVidCardEventId("123", 0);
    }

    @Test(expected = BaseCheckedException.class)
    public void testGetVidCardEventIdResidentServiceCheckedException() throws BaseCheckedException, IOException {
		ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper = new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
		Mockito.when(utilities.getUinByVid(Mockito.anyString())).thenThrow(new IOException());
		downloadCardService.getVidCardEventId("123", 0);
    }

    @Test
    public void testGetVidCardEventIdWithVidDetails() throws BaseCheckedException {
		ResponseWrapper<VidDownloadCardResponseDto> vidDownloadCardResponseDtoResponseWrapper = new ResponseWrapper<>();
        VidDownloadCardResponseDto vidDownloadCardResponseDto = new VidDownloadCardResponseDto();
        vidDownloadCardResponseDto.setStatus("success");
        vidDownloadCardResponseDtoResponseWrapper.setResponse(vidDownloadCardResponseDto);
		ResponseWrapper<ResidentCredentialResponseDto> responseWrapper = new ResponseWrapper<>();
        ResidentCredentialResponseDto residentCredentialResponseDto = new ResidentCredentialResponseDto();
        residentCredentialResponseDto.setId("123");
        residentCredentialResponseDto.setRequestId("123");
        responseWrapper.setResponse(residentCredentialResponseDto);
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
        ResponseWrapper<List<Map<String,?>>> vidResponse = new ResponseWrapper<>();
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
        Mockito.when(vidService.retrieveVids(Mockito.anyString(), Mockito.anyInt())).thenReturn(vidResponse);
        assertEquals("12345", downloadCardService.getVidCardEventId("123", 0).getT2());
    }

    @Test
    public void testGetIndividualIdStatus() throws ApisResourceAccessException, IOException {
        HashMap<String, String> packetStatusMap = new HashMap<>();
        packetStatusMap.put(ResidentConstants.AID_STATUS, PacketStatus.SUCCESS.name());
        packetStatusMap.put(ResidentConstants.TRANSACTION_TYPE_CODE, TransactionStage.CARD_READY_TO_DOWNLOAD.name());
        Mockito.when(utilities.getPacketStatus(Mockito.anyString())).thenReturn(packetStatusMap);
        ResponseWrapper<CheckStatusResponseDTO> getIndividualIdStatus = downloadCardService.getIndividualIdStatus("3425636374");
        assertEquals(PacketStatus.SUCCESS.name(),getIndividualIdStatus.getResponse().getAidStatus());
    }

}