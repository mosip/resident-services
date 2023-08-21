package io.mosip.resident.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.dto.AidStatusRequestDTO;
import io.mosip.resident.dto.AutnTxnDto;
import io.mosip.resident.dto.PageDto;
import io.mosip.resident.dto.RegistrationStatusDTO;
import io.mosip.resident.dto.RegistrationStatusResponseDTO;
import io.mosip.resident.dto.ServiceHistoryResponseDto;
import io.mosip.resident.entity.ResidentSessionEntity;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentSessionRepository;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;

/**
 * This class is used to test the get service history service
 * @author Kamesh Shekhar Prasad
 */

@RunWith(SpringRunner.class)
public class ResidentServiceGetServiceHistoryTest {
	
    private static final String LOCALE_EN_US = "en-US";

	@MockBean
    private ProxyIdRepoService proxyIdRepoService;

    @InjectMocks
    private ResidentServiceImpl residentServiceImpl;

    @Mock
    private IdentityServiceImpl identityServiceImpl;

    @Mock
    private ResidentTransactionRepository residentTransactionRepository;

    @Mock
    private RequestValidator validator;

    @Mock
    private Utilities utilities;

    @Mock
    private PartnerServiceImpl partnerServiceImpl;

    @Mock
    private IdAuthService idAuthServiceImpl;

    @Mock
    private Environment environment;

    @Mock
    private ResidentSessionRepository residentSessionRepository;

    @Mock
    private Utility utility;

    @Mock
    private TemplateUtil templateUtil;

    @Mock
    private ProxyMasterdataService proxyMasterdataService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ResidentServiceRestClient residentServiceRestClient;

    List<AutnTxnDto> details = null;

    private int pageStart;
    private int pageSize;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String serviceType;
    private String sortType;
    List<ServiceHistoryResponseDto> serviceHistoryResponseDto;
    private ArrayList<String> partnerIds;
    List<ResidentTransactionEntity> residentTransactionEntityList;
    ResidentTransactionEntity residentTransactionEntity;
    private String statusFilter;
    private String searchText;

    private ResponseWrapper<PageDto<ServiceHistoryResponseDto>> responseWrapper;

    private ResidentSessionEntity residentSessionEntity;

    private Query query;

    @Before
    public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException, IOException {
        statusFilter = EventStatus.SUCCESS.toString();
        searchText = "1";
        details = new ArrayList<>();
        pageSize = 10;
        pageStart = 2;
        serviceType = "AUTHENTICATION_REQUEST";
        sortType = "ASC";
        serviceHistoryResponseDto = new ArrayList<>();
        partnerIds = new ArrayList<>();
        residentTransactionEntityList = new ArrayList<>();
        residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setEventId("eventId");
        responseWrapper = new ResponseWrapper<>();
        residentTransactionEntity.setRequestTrnId("12345");
        residentTransactionEntity.setStatusComment("Success");
        residentTransactionEntity.setCrDtimes(LocalDateTime.now());
        residentTransactionEntity.setStatusCode(EventStatusSuccess.AUTHENTICATION_SUCCESSFULL.toString());
        residentTransactionEntityList.add(residentTransactionEntity);

        partnerIds.add("m-partner-default-auth");
        partnerIds.add("MOVP");

        query = Mockito.mock(Query.class);
        Mockito.when(entityManager.createNativeQuery(Mockito.anyString(), (Class) any())).thenReturn(query);
        Mockito.when(entityManager.createNativeQuery(Mockito.anyString())).thenReturn(query);
        Mockito.when(query.getSingleResult()).thenReturn(BigInteger.valueOf(1));
        Mockito.when(identityServiceImpl.getResidentIndvidualIdFromSession()).thenReturn("8251649601");
        Mockito.when(identityServiceImpl.getIDAToken(Mockito.anyString(), Mockito.anyString())).thenReturn("346697314566835424394775924659202696");
        Mockito.when(partnerServiceImpl.getPartnerDetails(Mockito.anyString())).thenReturn(partnerIds);

        Mockito.when(identityServiceImpl.getAvailableclaimValue(Mockito.anyString())).thenReturn("Kamesh");
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("property");
        residentSessionEntity = new ResidentSessionEntity();
        residentSessionEntity.setHost("localhost");
        Mockito.when(residentSessionRepository.findFirst2ByIdaTokenOrderByLoginDtimesDesc(
                Mockito.anyString())).thenReturn(List.of(residentSessionEntity));
        Mockito.when(identityServiceImpl.getResidentIdaToken()).thenReturn("1234");

        Mockito.when(templateUtil.getPurposeTemplateTypeCode(any(), any())).thenReturn("template-type-code");
        Mockito.when(templateUtil.getSummaryTemplateTypeCode(any(), any())).thenReturn("template-type-code");
        Mockito.when(templateUtil.getEventStatusTemplateTypeCode(Mockito.any())).thenReturn("template-type-code");
        Mockito.when(templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString())).thenReturn("success").thenReturn("Authentication is successful");
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("property");
        ReflectionTestUtils.setField(residentServiceImpl, "onlineVerificationPartnerId", "m-partner-default-auth");
    }

	private Page<Object[]> getPageData() {
		List<Object[]> entitiesList = new ArrayList<>();
		entitiesList = residentTransactionEntityList.stream().map(obj -> {
			Object[] objArr = new Object[12];
	        objArr[0] = obj.getEventId();
	        objArr[1] = obj.getRequestTypeCode();
	        objArr[2] = obj.getStatusCode();
	        objArr[3] = obj.getStatusComment();
	        objArr[4] = obj.getRefIdType();
	        objArr[5] = obj.getRefId();
	        objArr[6] = obj.getCrDtimes();
	        objArr[7] = obj.getUpdDtimes();
	        objArr[8] = obj.isReadStatus();
	        objArr[9] = obj.getPinnedStatus();
	        objArr[10] = obj.getPurpose();
	        objArr[11] = obj.getAttributeList();
	        return objArr;
		}).collect(Collectors.toList());
        Page<Object[]> page = new PageImpl<>(entitiesList);
        Mockito.when(residentTransactionRepository.findByTokenId(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyList(), Mockito.any())).thenReturn(page);
		return page;
	}

    @Test
    public void testGetServiceHistorySuccess() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 2;
        pageSize = 3;
        fromDate = LocalDate.now();
        toDate = LocalDate.now();
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                null, null, null, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(), serviceType, "DESC", statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testGetServiceHistoryDateNullCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 2;
        pageSize = 3;

        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null, serviceType, "DESC", statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null, serviceType, "DESC", statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());

    }

    @Test
    public void testGetServiceHistoryNullCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testGetServiceHistoryAllStatusFilter() throws ResidentServiceCheckedException, ApisResourceAccessException {
        fromDate = LocalDate.now();
        toDate = LocalDate.now();
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType,
                sortType, statusFilter, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceHistoryCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Integer pageStart = 1;
        Integer pageSize = 1;
        Mockito.when(residentServiceImpl.getServiceHistory( -1, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US)).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US)).thenThrow(ResidentServiceCheckedException.class);
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, 1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceHistory( -1, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(0, residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetServiceHistoryNegativeResidentServiceCheckedException() throws ResidentServiceCheckedException, ApisResourceAccessException {
        Integer pageStart = 1;
        Mockito.when(residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US)).thenThrow(ResidentServiceCheckedException.class);
        assertEquals(0, residentServiceImpl.getServiceHistory( pageStart, -1, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testPageSizeCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageSize = 10;
        pageStart = 1;
        assertEquals(10, residentServiceImpl.getServiceHistory(null, null, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(null, pageSize, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, null, fromDate, toDate, serviceType, sortType, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testSortTypeNullCheck() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 1;
        pageSize = 10;
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType,
                null, statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testServiceHistoryWithDifferentParameters() throws ResidentServiceCheckedException, ApisResourceAccessException {
        pageStart = 1;
        pageSize = 10;
        fromDate = LocalDate.MAX;
        toDate = LocalDate.MIN;
        serviceType = ServiceType.AUTHENTICATION_REQUEST.toString();
        sortType = "ASC";
        statusFilter = "SUCCESS";
        searchText = "a";
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType,
                statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                statusFilter, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, serviceType, sortType,
                null, "a", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                null, "a", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, null, sortType,
                statusFilter, "a", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, fromDate, toDate, "ALL", sortType,
                null, "a", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testGetServiceHistoryWithStatusFilterServiceType() throws ResidentServiceCheckedException, ApisResourceAccessException {
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(),
                ServiceType.ALL.toString(), sortType,
                "SUCCESS", null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                ServiceType.AUTHENTICATION_REQUEST.toString(), sortType,
                "In Progress", null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                ServiceType.AUTHENTICATION_REQUEST.toString(), sortType,
                null, "1", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                null, sortType,
                "SUCCESS", "1", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(),
                ServiceType.AUTHENTICATION_REQUEST.toString(), sortType,
                null, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(),
                null, sortType,
                null, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                ServiceType.AUTHENTICATION_REQUEST.toString(), sortType,
                null, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                null, sortType,
                "FAILED", null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                null, sortType,
                null, "123", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testGetServiceHistoryWithUpdatedDtimes() throws ResidentServiceCheckedException, ApisResourceAccessException {
        residentTransactionEntity.setUpdDtimes(LocalDateTime.now());
        residentTransactionEntity.setRequestTypeCode(RequestType.REVOKE_VID.name());
        residentTransactionEntityList.add(residentTransactionEntity);
        Page<Object[]> page = getPageData();
       assertEquals(10, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                null, sortType,
                null, "123", "eng", 0, LOCALE_EN_US).getResponse().getPageSize());

    }
    @Test
    public void testGetAidStatus() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId("10087100401001420220929210144");
        aidStatusRequestDTO.setOtp("111111");
        aidStatusRequestDTO.setTransactionId("1234567890");
        Mockito.when(idAuthServiceImpl.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(identityServiceImpl.getIndividualIdForAid(Mockito.anyString())).thenReturn("2476302389");
        assertEquals("PROCESSED", residentServiceImpl.getAidStatus(aidStatusRequestDTO, true).getAidStatus());
    }

    @Test
    public void testGetAidStatusOtpValidationFalse() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId("10087100401001420220929210144");
        aidStatusRequestDTO.setOtp("111111");
        aidStatusRequestDTO.setTransactionId("1234567890");
        Mockito.when(idAuthServiceImpl.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(identityServiceImpl.getIndividualIdForAid(Mockito.anyString())).thenReturn("2476302389");
        assertEquals("PROCESSED", residentServiceImpl.getAidStatus(aidStatusRequestDTO, false).getAidStatus());
    }

    @Test
    public void testGetAidStatusOtpValidationException() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId("10087100401001420220929210144");
        aidStatusRequestDTO.setOtp("111111");
        aidStatusRequestDTO.setTransactionId("1234567890");
        Mockito.when(idAuthServiceImpl.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(identityServiceImpl.getIndividualIdForAid(Mockito.anyString())).thenReturn(null);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        RegistrationStatusResponseDTO registrationStatusResponseDTO = new RegistrationStatusResponseDTO();
        RegistrationStatusDTO registrationStatusDTO = new RegistrationStatusDTO();
        registrationStatusDTO.setStatusCode("FAILED");
        registrationStatusResponseDTO.setResponse(List.of(registrationStatusDTO));
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(registrationStatusResponseDTO);
        assertEquals("UNDER PROCESSING", residentServiceImpl.getAidStatus(aidStatusRequestDTO, false).getAidStatus());
    }

    @Test(expected = ResidentServiceCheckedException.class)
    public void testGetAidStatusOtpValidationResidentServiceCheckedException() throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
        AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
        aidStatusRequestDTO.setIndividualId("10087100401001420220929210144");
        aidStatusRequestDTO.setOtp("111111");
        aidStatusRequestDTO.setTransactionId("1234567890");
        Mockito.when(idAuthServiceImpl.validateOtp(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(identityServiceImpl.getIndividualIdForAid(Mockito.anyString())).thenReturn(null);
        Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Mockito.when(residentServiceRestClient.postApi(any(), any(), any(), any())).thenReturn(null);
        assertEquals("UNDER PROCESSING", residentServiceImpl.getAidStatus(aidStatusRequestDTO, false).getAidStatus());
    }

    @Test
    public void testGetUserinfo() throws ApisResourceAccessException {
        assertEquals("Kamesh",
                residentServiceImpl.getUserinfo("ida_token", 0, LOCALE_EN_US).getResponse().getFullName());
    }

    @Test
    public void testGetUserinfoMultipleLoginTime() throws ApisResourceAccessException {
        Mockito.when(residentSessionRepository.findFirst2ByIdaTokenOrderByLoginDtimesDesc(
                Mockito.anyString())).thenReturn(List.of(residentSessionEntity, residentSessionEntity));
        assertEquals("Kamesh",
                residentServiceImpl.getUserinfo("ida_token", 0, LOCALE_EN_US).getResponse().getFullName());
    }

    @Test
    public void testGetFileName(){
        Mockito.when(utility.getFileName(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn("Ack");
        assertEquals("Ack", residentServiceImpl.getFileName("123", IdType.UIN, 0, LOCALE_EN_US));
    }

    @Test
    public void testGetSummaryForLangCode() throws ResidentServiceCheckedException {
        Mockito.when(templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString()))
                .thenReturn("Success");
        residentServiceImpl.getSummaryForLangCode(residentTransactionEntity, "eng", "SUCCESS",
                RequestType.AUTHENTICATION_REQUEST);
    }

    @Test
    public void testGetSummaryForLangCodeFailure() throws ResidentServiceCheckedException {
        Mockito.when(templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(Mockito.anyString(), Mockito.anyString()))
                .thenReturn("Success");
        residentServiceImpl.getSummaryForLangCode(residentTransactionEntity, "eng", "Failed",
                RequestType.AUTHENTICATION_REQUEST);
    }

    @Test
    public void testGetServiceHistorySuccessWithUpdatedTimes() throws ResidentServiceCheckedException, ApisResourceAccessException {
        residentTransactionEntity.setStatusCode(EventStatusSuccess.AUTHENTICATION_SUCCESSFULL.toString());
        residentTransactionEntity.setCrDtimes(LocalDateTime.now().minusMinutes(1));
        residentTransactionEntity.setUpdDtimes(LocalDateTime.now());
        residentTransactionEntityList.add(residentTransactionEntity);
        Page<Object[]> page = getPageData();
        pageStart = 2;
        pageSize = 3;
        fromDate = LocalDate.now();
        toDate = LocalDate.now();
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                null, null, null, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(), serviceType, "DESC", statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }

    @Test
    public void testGetServiceHistorySuccessWithServiceTypeALL() throws ResidentServiceCheckedException, ApisResourceAccessException {
        residentTransactionEntity.setStatusCode(EventStatusSuccess.AUTHENTICATION_SUCCESSFULL.toString());
        residentTransactionEntity.setCrDtimes(LocalDateTime.now().minusMinutes(1));
        residentTransactionEntity.setUpdDtimes(LocalDateTime.now());
        residentTransactionEntityList.add(residentTransactionEntity);
        Page<Object[]> page = getPageData();
        pageStart = 2;
        pageSize = 3;
        fromDate = LocalDate.now();
        toDate = LocalDate.now();
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, null, null,
                null, null, null, null, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
        assertEquals(3, residentServiceImpl.getServiceHistory(pageStart, pageSize, LocalDate.now(), LocalDate.now(), "ALL", "DESC", statusFilter, searchText, "eng", 0, LOCALE_EN_US).getResponse().getPageSize());
    }
    @Test(expected = Exception.class)
    public void reqUinUpdateValidateAuthIndividualIdWithUINFailure() throws Exception {
        ReflectionTestUtils.invokeMethod(residentServiceImpl, "validateAuthIndividualIdWithUIN", "1234", "RID", null, null);
    }

    @Test
    public void testGetIdType(){
        assertEquals(IdType.RID,ReflectionTestUtils.invokeMethod(residentServiceImpl, "getIdType", "RID"));
    }

    @Test
    public void testGetIdTypeNull(){
        assertNull(ReflectionTestUtils.invokeMethod(residentServiceImpl, "getIdType", "aid"));
    }
}
