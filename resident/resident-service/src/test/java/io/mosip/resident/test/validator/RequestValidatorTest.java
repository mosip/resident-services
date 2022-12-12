package io.mosip.resident.test.validator;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
public class RequestValidatorTest {

	@Mock
	private UinValidator<String> uinValidator;

	@Mock
	private VidValidator<String> vidValidator;

	@Mock
	private RidValidator<String> ridValidator;

	@InjectMocks
	private RequestValidator requestValidator;

	@Mock
	private AuditUtil audit;

	@Mock
	private Environment environment;

	String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	@InjectMocks
	private ResidentService residentService = new ResidentServiceImpl();

	@Before
	public void setup() {
		Mockito.when(uinValidator.validateId(Mockito.any())).thenReturn(true);
		Map<RequestIdType, String> map = new HashMap<RequestIdType, String>();
		map.put(RequestIdType.RE_PRINT_ID, "mosip.resident.print");
		map.put(RequestIdType.AUTH_LOCK_ID, "mosip.resident.authlock");
		map.put(RequestIdType.AUTH_UNLOCK_ID, "mosip.resident.authunlock");
		map.put(RequestIdType.E_UIN_ID, "mosip.resident.euin");
		map.put(RequestIdType.AUTH_HISTORY_ID, "mosip.resident.authhistory");
		map.put(RequestIdType.RES_UPDATE, "mosip.resident.updateuin");
		map.put(RequestIdType.CHECK_STATUS, "mosip.resident.checkstatus");
		ReflectionTestUtils.setField(requestValidator, "checkStatusID", "mosip.resident.checkstatus");
		ReflectionTestUtils.setField(requestValidator, "authLockId", "mosip.resident.authlock");
		ReflectionTestUtils.setField(requestValidator, "euinId", "mosip.resident.euin");
		ReflectionTestUtils.setField(requestValidator, "authHstoryId", "mosip.resident.authhistory");
		ReflectionTestUtils.setField(requestValidator, "uinUpdateId", "mosip.resident.updateuin");
		ReflectionTestUtils.setField(requestValidator, "authTypes", "bio-FIR,bio-IIR");
		ReflectionTestUtils.setField(requestValidator, "version", "v1");
		ReflectionTestUtils.setField(requestValidator, "reqResVersion", "1.0");
		ReflectionTestUtils.setField(requestValidator, "map", map);
		ReflectionTestUtils.setField(requestValidator, "authTypes", "otp,bio-FIR,bio-IIR,bio-FACE");
		ReflectionTestUtils.setField(residentService, "authTypes", "otp,bio-FIR,bio-IIR,bio-FACE");
		ReflectionTestUtils.setField(requestValidator, "mandatoryLanguages", "eng");
		ReflectionTestUtils.setField(requestValidator, "optionalLanguages", "ara");
		Mockito.when(uinValidator.validateId(Mockito.anyString())).thenReturn(true);
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		Mockito.when(ridValidator.validateId(Mockito.anyString())).thenReturn(true);
		Mockito.when(environment.getProperty(Mockito.anyString())).thenReturn("property");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidId() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authLockRequestDto);
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidUnlockId() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authLockRequestDto);
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.UNLOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValideuinId() throws Exception {
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthHistoryId() throws Exception {
		AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authHistoryRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authlock");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidVersion() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthHistoryVersion() throws Exception {
		AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authHistoryRequestDTO);
		requestWrapper.setVersion("v2");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValideuinVersion() throws Exception {
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v2");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidRequest() throws Exception {

		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(null);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthHistoryRequest() throws Exception {

		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestWrapper.setRequest(null);
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValideuinRequest() throws Exception {

		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestWrapper.setRequest(null);
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidIndividualType() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidIndividualId() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.any())).thenReturn(false);
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualId("12345");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test
	public void testValidateAuthLockOrUnlockRequestSuccess() throws Exception{
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualId("12345");
		authLockRequestDto.setOtp("12345");
		List<String> authTypes = new ArrayList<>();
		authTypes.add("bio-FIR");
		authLockRequestDto.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
	}

	@Test
	public void testValidateAuthLockOrUnlockRequestSuccessForUnlock() throws Exception{
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualId("12345");
		authLockRequestDto.setOtp("12345");
		List<String> authTypes = new ArrayList<>();
		authTypes.add("bio-FIR");
		authLockRequestDto.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.UNLOCK);
	}

	@Test(expected = InvalidInputException.class)
	public void testeuinValidIndividualType() throws Exception {
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		euinRequestDTO.setIndividualIdType(IdType.RID.name());
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidIndividualType() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidOtp() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualId("12344567");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthTypes() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setOtp("1232354");
		authLockRequestDto.setIndividualId("12344567");
		List<String> authTypes = new ArrayList<String>();
		authTypes.add("bio-FMR");
		authLockRequestDto.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidEmptyAuthTypes() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setOtp("1232354");
		authLockRequestDto.setIndividualId("12344567");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidPageFetch() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageStart("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidPageStart() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidIndividualId() throws Exception {
		Mockito.when(uinValidator.validateId(Mockito.anyString())).thenReturn(false);
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidpageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1Q");
		authRequestDTO.setPageStart("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidpageStart() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1");
		authRequestDTO.setPageStart("1Q");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidpageStartPageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch(" ");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		authRequestDTO.setPageStart(" ");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidPageStartnullPageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		authRequestDTO.setPageStart(" ");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidnullPageStartPageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch(" ");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryinValidpageStartPageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("-10");
		authRequestDTO.setPageStart("-11");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testeuinValidIndividualId() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(false);
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		euinRequestDTO.setIndividualIdType(IdType.VID.name());
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test
	public void testValidateRequest() {
		ResidentReprintRequestDto request = new ResidentReprintRequestDto();
		request.setIndividualId("3542102");
		request.setIndividualIdType(IdType.UIN.name());
		request.setOtp("1234");
		request.setTransactionID("9876543210");
		RequestWrapper<ResidentReprintRequestDto> reqWrapper = new RequestWrapper<>();
		reqWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		reqWrapper.setRequest(request);
		reqWrapper.setId("mosip.resident.print");
		reqWrapper.setVersion("v1");
		requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

	}

	@Test(expected = InvalidInputException.class)
	public void testvalidateRequestInValidId() {

		RequestWrapper<ResidentReprintRequestDto> reqWrapper = new RequestWrapper<>();
		reqWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));

		requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

		reqWrapper.setId("mosip.resident.print1");

		requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

		reqWrapper.setVersion("v1");

		requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

		reqWrapper.setId("mosip.resident.print");
		reqWrapper.setVersion("v2");
		requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);

	}

	@Test
	public void testInvalidDateTime() {
		boolean result = false;
		ResidentReprintRequestDto request = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> reqWrapper = new RequestWrapper<>();
		reqWrapper.setRequest(request);
		reqWrapper.setId("mosip.resident.print");
		reqWrapper.setVersion("v1");
		try {
			requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);
		} catch (InvalidInputException e) {
			assertTrue(e.getMessage().contains("Invalid Input Parameter- requesttime"));
			result = true;
		}
		if (!result)
			fail();
	}

	@Test(expected = InvalidInputException.class)
	public void testeuinValidCardType() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		euinRequestDTO.setIndividualIdType(IdType.VID.name());
		euinRequestDTO.setIndividualId("1234567");
		euinRequestDTO.setCardType("vid");
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testeuinValidOtp() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		euinRequestDTO.setIndividualIdType(IdType.VID.name());
		euinRequestDTO.setIndividualId("1234567");
		euinRequestDTO.setCardType(CardType.MASKED_UIN.name());
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testeuinValidTransactionId() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		euinRequestDTO.setIndividualIdType(IdType.VID.name());
		euinRequestDTO.setIndividualId("1234567");
		euinRequestDTO.setCardType(CardType.MASKED_UIN.name());
		euinRequestDTO.setOtp("12345");
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test
	public void testValidateEuinRequestSuccess() throws Exception{
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		euinRequestDTO.setIndividualIdType(IdType.VID.name());
		euinRequestDTO.setIndividualId("1234567");
		euinRequestDTO.setCardType(CardType.MASKED_UIN.name());
		euinRequestDTO.setOtp("12345");
		euinRequestDTO.setTransactionID("9876543210");
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidTransactionId() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualId("12344567");
		authLockRequestDto.setOtp("12345");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidGetStatusId() throws Exception {
		RequestDTO requestDTO = new RequestDTO();
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testGetStatusValidIndividualType() throws Exception {
		RequestDTO requestDTO = new RequestDTO();
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();

		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidateRidCheckStatusRequestDTONullRequest() throws Exception{
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(null);
		requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateRidCheckStatusRequestDTOInvalidIdType() throws Exception{
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setIndividualIdType("Invalid");
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateRidCheckStatusRequestDTONullIndividualId() throws Exception{
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setIndividualIdType(IdType.RID.name());
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
	}

	@Test
	public void testValidateRidCheckStatusRequestDTOSuccess() throws Exception{
		RequestDTO requestDTO = new RequestDTO();
		requestDTO.setIndividualIdType(IdType.RID.name());
		requestDTO.setIndividualId("1234567");
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testGetStatusValidIndividualId() throws Exception {
		Mockito.when(ridValidator.validateId(Mockito.anyString())).thenReturn(false);
		RequestDTO requestDTO = new RequestDTO();
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("INVALID_RID");
		requestDTO.setIndividualId("123456");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRidCheckStatusRequestDTO(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidUpdateId() throws Exception {
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidIndividualType() throws Exception {
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();

		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidIndividualId() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(false);
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidTransaction() throws Exception {

		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setOtp("1234567");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidOtp() throws Exception {

		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidIdentityJson() throws Exception {

		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setOtp("1234567");
		requestDTO.setTransactionID("1234567");
		requestDTO.setIdentityJson("");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidReprintId() throws Exception {
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidUpdateRequestNullRequest() throws Exception{
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestValidator.validateUpdateRequest(requestWrapper, false);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidUpdateRequestIndividualIdNull() throws Exception{
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		requestDTO.setIndividualIdType("VID");
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);
	}

	@Test
	public void testValidUpdateRequestIsPatchTrue() throws Exception{
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setOtp("1234567");
		requestDTO.setTransactionID("1234567");
		requestDTO.setIdentityJson("");
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, true);
	}

	@Test
	public void testValidateUpdateRequest() throws Exception{
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setOtp("1234567");
		requestDTO.setTransactionID("1234567");
		requestDTO.setIdentityJson("abcdef");
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);
	}

	@Test
	public void testValidateUpdateRequestV2() throws Exception{
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		requestDTO.setIndividualIdType("UIN");
		requestDTO.setIndividualId("1234567");
		requestDTO.setOtp("1234567");
		requestDTO.setTransactionID("1234567");
		requestDTO.setIdentityJson("abcdef");
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper, false);
	}

	@Test(expected = InvalidInputException.class)
	public void testReprintValidIndividualType() throws Exception {
		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();

		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testReprintValidIndividualId() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(false);
		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testReprintNullIndividualId() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test
	public void testReprintSuccess() throws Exception{
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("12345678");
		requestDTO.setCardType(CardType.MASKED_UIN.name());
		requestDTO.setOtp("111111");
		requestDTO.setTransactionID("11111");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testReprintValidTransaction() throws Exception {

		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setOtp("1234567");
		requestDTO.setCardType(CardType.MASKED_UIN.name());
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testReprintValidOtp() throws Exception {

		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setCardType(CardType.MASKED_UIN.name());
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testReprintValidCardType() throws Exception {

		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setCardType("VID");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateReprintRequestNullRequest() throws Exception{

		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(null);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidOtp() throws Exception
	{
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("1234567");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidTransactionId() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("1234567");
		authRequestDTO.setOtp("1245");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test
	public void testValidateAuthHistoryRequestSuccess() throws Exception{
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualId("1234567");
		authRequestDTO.setOtp("1245");
		authRequestDTO.setTransactionID("1234567");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testAuthUnlockRequestNull() throws Exception {
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidIndividualIdForAuthUnlockRequest() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.any())).thenReturn(false);
		AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
		authUnLockRequestDto.setTransactionID("12345");
		authUnLockRequestDto.setIndividualId("12345");
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authUnLockRequestDto);
		requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthUnlockRequestUnlockForSecondsNull() throws Exception{
		AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
		authUnLockRequestDto.setTransactionID("12345");
		authUnLockRequestDto.setIndividualId("12345");
		authUnLockRequestDto.setOtp("12345");
		authUnLockRequestDto.setUnlockForSeconds(null);
		List<String> authTypeList = new ArrayList<>();
		authTypeList.add("bio-FIR");
		authUnLockRequestDto.setAuthType(authTypeList);
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authUnLockRequestDto);
		requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
	}

	@Test
	public void testValidateAuthUnlockRequestSuccess() throws Exception{
		AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
		authUnLockRequestDto.setTransactionID("12345");
		authUnLockRequestDto.setIndividualId("12345");
		authUnLockRequestDto.setOtp("12345");
		authUnLockRequestDto.setUnlockForSeconds("10");
		List<String> authTypeList = new ArrayList<>();
		authTypeList.add("bio-FIR");
		authUnLockRequestDto.setAuthType(authTypeList);
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authUnLockRequestDto);
		requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthUnlockRequestNullIndividualId() throws Exception {
		AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
		authUnLockRequestDto.setTransactionID("12345");
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authUnLockRequestDto);
		requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidOtpForAuthUnlockRequest() throws Exception {
		AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
		authUnLockRequestDto.setTransactionID("12345");
		authUnLockRequestDto.setIndividualId("12344567");
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authUnLockRequestDto);
		requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidTransactionIdForAuthUnlockRequest() throws Exception {
		AuthUnLockRequestDTO authUnLockRequestDto = new AuthUnLockRequestDTO();
		authUnLockRequestDto.setIndividualId("12344567");
		authUnLockRequestDto.setOtp("12345");
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authunlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authUnLockRequestDto);
		requestValidator.validateAuthUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}


	@Test(expected = InvalidInputException.class)
	public void testValidunlockForMinutesLessThanZeroForAuthUnlockRequest() throws Exception {
		AuthUnLockRequestDTO authUnLockRequestDto1 = new AuthUnLockRequestDTO();
		authUnLockRequestDto1.setIndividualId("12344567");
		authUnLockRequestDto1.setOtp("12345");
		authUnLockRequestDto1.setTransactionID("12345");
		authUnLockRequestDto1.setUnlockForSeconds(String.valueOf(-1L));
		List<String> authTypes = new ArrayList<String>();
		authTypes.add("bio-FIR");
		authUnLockRequestDto1.setAuthType(authTypes);
		RequestWrapper<AuthUnLockRequestDTO> requestWrapper1 = new RequestWrapper<>();
		requestWrapper1.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper1.setId("mosip.resident.authunlock");
		requestWrapper1.setVersion("v1");
		requestWrapper1.setRequest(authUnLockRequestDto1);
		requestValidator.validateAuthUnlockRequest(requestWrapper1, AuthTypeStatus.LOCK);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2BadRequest() throws Exception{
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(null);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2InvalidIndividualIdBadIdType() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2InvalidAuthType() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		authLockOrUnLockRequestDtoV2.setAuthTypes(null);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2BadAuthType() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("dummy");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test
	public void testValidateAuthLockOrUnlockRequestV2() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.auth.lock.unlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2EmptyAuthType() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2NegativeUnlockForSeconds() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(-1L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}
	
	@Test
	public void testValidateAuthLockOrUnlockRequestV2NullUnlockForSeconds() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(null);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.auth.lock.unlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
		//Should not throw exception
	}
	
	@Test
	public void testValidateAuthLockOrUnlockRequestV2PositiveUnlockForSeconds() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "authLockStatusUpdateV2Id", "mosip.resident.auth.lock.unlock");
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(1L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.auth.lock.unlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
		//Should not throw exception
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2EmptyAuthTypeV2() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2BadUnlockForSeconds2() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		List<AuthTypeStatusDtoV2> authTypes = new ArrayList<>();
		AuthTypeStatusDtoV2 authTypeStatusDto = new AuthTypeStatusDtoV2();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(-10L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthTypes(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}



	@Test(expected = InvalidInputException.class)
	public void testValidateAidStatusRequestDto() throws Exception{
		RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(null);
		requestValidator.validateAidStatusRequestDto(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAidStatusRequestDtoAidNull() throws Exception{
		AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
		aidStatusRequestDTO.setAid(null);
		RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(aidStatusRequestDTO);
		requestValidator.validateAidStatusRequestDto(requestWrapper);
	}

	@Test
	public void testValidateAidStatusRequestDtoSuccess() throws Exception{
		AidStatusRequestDTO aidStatusRequestDTO = new AidStatusRequestDTO();
		aidStatusRequestDTO.setAid("17");
		RequestWrapper<AidStatusRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(aidStatusRequestDTO);
		requestValidator.validateAidStatusRequestDto(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateChannelVerificationStatus() throws Exception{
		String channel ="";
		String individualId ="";
		requestValidator.validateChannelVerificationStatus(channel, individualId);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateChannelVerificationStatusNullChannel() throws Exception{
		String channel ="BadChannel";
		String individualId ="";
		requestValidator.validateChannelVerificationStatus(channel, individualId);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateChannelVerificationStatusNullIndividualId() throws Exception{
		String channel ="PHONE";
		requestValidator.validateChannelVerificationStatus(channel, null);
	}

	@Test
	public void testValidateChannelVerificationStatusSuccess() throws Exception{
		String channel ="PHONE";
		requestValidator.validateChannelVerificationStatus(channel, "12345678");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateServiceHistoryRequest() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "";
		String serviceType = "";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, sortType, sortType);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateServiceHistoryRequestBadServiceType() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "";
		String serviceType = "BadServiceType";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, sortType, sortType);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateServiceHistoryRequestBadSortType() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "BadSortType";
		String serviceType = "DATA_SHARE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, sortType, sortType);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateServiceHistoryRequestNullSortType() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String serviceType = "DATA_SHARE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, null, null, serviceType);
	}

	@Test
	public void testValidateServiceHistoryRequestDateCheck() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "DATA_SHARE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryServiceRequest() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "SERVICE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryID_MANAGEMENT_REQUEST() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "ID_MANAGEMENT_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryDATA_UPDATE_REQUEST() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "DATA_UPDATE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryAUTHENTICATION_REQUEST() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "AUTHENTICATION_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistorySuccess() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "DESC";
		String serviceType = "AUTHENTICATION_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType, null);
	}


	@Test(expected = InvalidInputException.class)
	public void testValidateIndividualId() throws Exception{
		String individualId = "";
		requestValidator.validateIndividualId(individualId);
	}

	@Test
	public void testValidateIndividualIdSuccess() throws Exception {
		String individualId = "123456789";
		requestValidator.validateIndividualId(individualId);
	}

	@Test
	public void testPhoneValidator() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "phoneRegex", "^([6-9]{1})([0-9]{9})$");
		String phone = "1234567890";
		requestValidator.phoneValidator(phone);
	}

	@Test
	public void testEmailValidator() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "emailRegex", "^[a-zA-Z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+\\.[a-zA-Z]{2,4}$");
		String email = "abc@gmail.com";
		requestValidator.emailValidator(email);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateRevokeVidRequestWrapperRevokeVidNull() throws Exception{
		BaseVidRevokeRequestDTO baseVidRevokeRequestDTO = new BaseVidRevokeRequestDTO();
		baseVidRevokeRequestDTO.setVidStatus("REVOKED");
		baseVidRevokeRequestDTO.setTransactionID("123456789");
		RequestWrapper<BaseVidRevokeRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime("2019-01-0");
		requestWrapper.setId("mosip.resident.revokevid");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(baseVidRevokeRequestDTO);
		requestValidator.validateRevokeVidRequestWrapper(requestWrapper, "v1");
	}

	@Test(expected = InvalidInputException.class)
	public void	 testValidateRevokeVidRequestWrapper() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "revokeVidId", "mosip.resident.vidstatus");
		BaseVidRevokeRequestDTO baseVidRevokeRequestDTO = new BaseVidRevokeRequestDTO();
		baseVidRevokeRequestDTO.setVidStatus("mosip.resident.vidstatus");
		baseVidRevokeRequestDTO.setTransactionID("123456789");
		RequestWrapper<BaseVidRevokeRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime("2019-01-0");
		requestWrapper.setId("mosip.resident.vidstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(baseVidRevokeRequestDTO);
		requestValidator.validateRevokeVidRequestWrapper(requestWrapper, "v1");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateId() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "transliterateId", "mosip.resident.transliteration.transliterate");
		MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
		TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
		requestDTO.setId(null);
		requestValidator.validateId(requestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateTransliterationId() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "transliterateId", "mosip.resident.transliteration.transliterate");
		MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
		TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
		requestDTO.setId("mosip");
		requestValidator.validateId(requestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateRequestNewApi() throws Exception{
		RequestWrapper<?> request = new RequestWrapper<>();
		RequestIdType requestIdType = RequestIdType.RE_PRINT_ID;
		requestValidator.validateRequestNewApi(request, requestIdType);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateRequestNewApiInvalidId() throws Exception{
		RequestWrapper<?> request = new RequestWrapper<>();
		RequestIdType requestIdType = RequestIdType.VERSION;
		requestValidator.validateRequestNewApi(request, requestIdType);
	}

	@Test
	public void testValidateDownloadCardVid() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "reprintId", "mosip.resident.print");
		requestValidator.validateDownloadCardVid("12345");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardVidFailed() throws Exception{
		Mockito.when(vidValidator.validateId(Mockito.any())).thenReturn(false);
		ReflectionTestUtils.setField(requestValidator, "reprintId", "mosip.resident.print");
		requestValidator.validateDownloadCardVid("12345");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadPersonalizedCard() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
		mainRequestDTO.setId("id");
		DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
		mainRequestDTO.setRequest(downloadPersonalizedCardDto);
		requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadPersonalizedCardNullId() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
		mainRequestDTO.setId(null);
		DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
		mainRequestDTO.setRequest(downloadPersonalizedCardDto);
		requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadPersonalizedCardNullRequestTime() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
		mainRequestDTO.setId("property");
		DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
		mainRequestDTO.setRequest(downloadPersonalizedCardDto);
		requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadPersonalizedCardNullString() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
		mainRequestDTO.setId("property");
		mainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
		mainRequestDTO.setRequest(downloadPersonalizedCardDto);
		requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
	}

	@Test
	public void testValidateDownloadPersonalizedCardSuccess() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
		mainRequestDTO.setId("property");
		mainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
		downloadPersonalizedCardDto.setHtml("html");
		mainRequestDTO.setRequest(downloadPersonalizedCardDto);
		requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadPersonalizedCardBadHtml() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadPersonalizedCardDto> mainRequestDTO = new io.mosip.resident.dto.MainRequestDTO<>();
		mainRequestDTO.setId("property");
		mainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		DownloadPersonalizedCardDto downloadPersonalizedCardDto = new DownloadPersonalizedCardDto();
		downloadPersonalizedCardDto.setHtml("`1&`");
		mainRequestDTO.setRequest(downloadPersonalizedCardDto);
		requestValidator.validateDownloadPersonalizedCard(mainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardNullTransactionId() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardNonNumericTransactionId() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTO.setTransactionId("ab");
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardLessThan10DigitTransactionId() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTO.setTransactionId("1234");
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardNullOtp() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTO.setTransactionId("1234343434");
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardNonNumericOtp() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTO.setTransactionId("1234343434");
		downloadCardRequestDTO.setOtp("abc");
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardInvalidIndividualId() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTO.setTransactionId("1234343434");
		downloadCardRequestDTO.setOtp("111111");
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateDownloadCardEmptyIndividualId() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTO.setTransactionId("1234343434");
		downloadCardRequestDTO.setOtp("111111");
		downloadCardRequestDTO.setIndividualId("");
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test
	public void testValidateDownloadCardSuccess() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO =
				new io.mosip.resident.dto.MainRequestDTO<>();
		DownloadCardRequestDTO downloadCardRequestDTO = new DownloadCardRequestDTO();
		downloadCardRequestDTO.setTransactionId("1234343434");
		downloadCardRequestDTO.setOtp("111111");
		downloadCardRequestDTO.setIndividualId("123");
		downloadCardRequestDTOMainRequestDTO.setId("property");
		downloadCardRequestDTOMainRequestDTO.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		downloadCardRequestDTOMainRequestDTO.setRequest(downloadCardRequestDTO);
		requestValidator.validateDownloadCardRequest(downloadCardRequestDTOMainRequestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateUpdateDataRequestInvalidUserId() throws Exception{
		io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
				new io.mosip.resident.dto.MainRequestDTO<>();
		OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
		otpRequestDTOV3.setOtp("111111");
		otpRequestDTOV3.setTransactionID("1232323232");
		userIdOtpRequest.setId("property");
		userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		userIdOtpRequest.setRequest(otpRequestDTOV3);
		requestValidator.validateUpdateDataRequest(userIdOtpRequest);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateUpdateDataRequestInvalidPhoneUserId() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "emailRegex", "^[a-zA-Z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+\\.[a-zA-Z]{2,4}$");
		ReflectionTestUtils.setField(requestValidator, "phoneRegex", "^([6-9]{1})([0-9]{9})$");
		io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
				new io.mosip.resident.dto.MainRequestDTO<>();
		OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
		otpRequestDTOV3.setOtp("111111");
		otpRequestDTOV3.setTransactionID("1232323232");
		userIdOtpRequest.setId("property");
		otpRequestDTOV3.setUserId("k");
		userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		userIdOtpRequest.setRequest(otpRequestDTOV3);
		requestValidator.validateUpdateDataRequest(userIdOtpRequest);
	}

	@Test
	public void testValidateUpdateDataRequestCorrectPhoneUserId() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "emailRegex", "^[a-zA-Z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+\\.[a-zA-Z]{2,4}$");
		ReflectionTestUtils.setField(requestValidator, "phoneRegex", "^([6-9]{1})([0-9]{9})$");
		io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
				new io.mosip.resident.dto.MainRequestDTO<>();
		OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
		otpRequestDTOV3.setOtp("111111");
		otpRequestDTOV3.setTransactionID("1232323232");
		userIdOtpRequest.setId("property");
		otpRequestDTOV3.setUserId("8878787878");
		userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		userIdOtpRequest.setRequest(otpRequestDTOV3);
		requestValidator.validateUpdateDataRequest(userIdOtpRequest);
	}

	@Test
	public void testValidateUpdateDataRequestCorrectEmailId() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "emailRegex", "^[a-zA-Z0-9_\\-\\.]+@[a-zA-Z0-9_\\-]+\\.[a-zA-Z]{2,4}$");
		ReflectionTestUtils.setField(requestValidator, "phoneRegex", "^([6-9]{1})([0-9]{9})$");
		io.mosip.resident.dto.MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest =
				new io.mosip.resident.dto.MainRequestDTO<>();
		OtpRequestDTOV3 otpRequestDTOV3 = new OtpRequestDTOV3();
		otpRequestDTOV3.setOtp("111111");
		otpRequestDTOV3.setTransactionID("1232323232");
		userIdOtpRequest.setId("property");
		otpRequestDTOV3.setUserId("test@g.com");
		userIdOtpRequest.setRequesttime(new Date(2012, 2, 2, 2, 2,2));
		userIdOtpRequest.setRequest(otpRequestDTOV3);
		requestValidator.validateUpdateDataRequest(userIdOtpRequest);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateVidCreateRequest(){
		requestValidator.validateVidCreateRequest(null, false, null);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateVidCreateV2Request(){
		ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
		requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
		requestValidator.validateVidCreateV2Request(requestDto,
				false, null);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateVidCreateV2RequestInvalidVersion(){
		ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
		ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
		requestDto.setId("generate");
		requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
		requestValidator.validateVidCreateV2Request(requestDto,
				false, null);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateVidCreateV2RequestInvalidRequest(){
		ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
		ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
		ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
		requestDto.setId("generate");
		requestDto.setVersion("newVersion");
		requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
		requestValidator.validateVidCreateV2Request(requestDto,
				false, null);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateVidCreateV2RequestInvalidIndividualId(){
		ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
		ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
		ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
		requestDto.setId("generate");
		requestDto.setVersion("newVersion");
		VidRequestDtoV2 vidRequestDtoV2 = new VidRequestDtoV2();
		vidRequestDtoV2.setVidType("PERPETUAL");
		requestDto.setRequest(vidRequestDtoV2);
		requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
		requestValidator.validateVidCreateV2Request(requestDto,
				false, null);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateVidCreateV2RequestInvalidTransactionId(){
		ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
		ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
		ResidentVidRequestDtoV2 requestDto = new ResidentVidRequestDtoV2();
		requestDto.setId("generate");
		requestDto.setVersion("newVersion");
		VidRequestDtoV2 vidRequestDtoV2 = new VidRequestDtoV2();
		vidRequestDtoV2.setVidType("PERPETUAL");
		requestDto.setRequest(vidRequestDtoV2);
		requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
		requestValidator.validateVidCreateV2Request(requestDto,
				false, "123");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateVidCreateV2RequestInvalidRequestDto(){
		ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
		ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
		ResidentVidRequestDto requestDto = new ResidentVidRequestDto();
		requestDto.setId("generate");
		requestDto.setVersion("newVersion");
		VidRequestDto vidRequestDtoV2 = new VidRequestDto();
		vidRequestDtoV2.setVidType("PERPETUAL");
		requestDto.setRequest(vidRequestDtoV2);
		requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
		requestValidator.validateVidCreateV2Request(requestDto,
				true, "123");
	}

	@Test
	public void testValidateVidCreateV2RequestSuccess(){
		ReflectionTestUtils.setField(requestValidator, "generateId", "generate");
		ReflectionTestUtils.setField(requestValidator, "newVersion", "newVersion");
		ResidentVidRequestDto requestDto = new ResidentVidRequestDto();
		requestDto.setId("generate");
		requestDto.setVersion("newVersion");
		VidRequestDto vidRequestDtoV2 = new VidRequestDto();
		vidRequestDtoV2.setVidType("PERPETUAL");
		vidRequestDtoV2.setTransactionID("1232323232");
		requestDto.setRequest(vidRequestDtoV2);
		requestDto.setRequesttime(String.valueOf(LocalDateTime.now()));
		requestValidator.validateVidCreateV2Request(requestDto,
				false, "123");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidatePageFetchAndPageStartFormat(){
		RequestWrapper<AuthHistoryRequestDTO> requestDTO = new RequestWrapper<>();
		AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
		authHistoryRequestDTO.setPageStart(String.valueOf(0));
		authHistoryRequestDTO.setPageFetch("0");
		requestDTO.setRequest(authHistoryRequestDTO);
		requestValidator.validatePageFetchAndPageStartFormat(requestDTO, "request");
	}

	@Test
	public void testValidateVid(){
		Mockito.when(vidValidator.validateId(Mockito.any())).thenThrow(new InvalidIDException(ResidentErrorCode.INVALID_VID.getErrorCode(),
				ResidentErrorCode.INVALID_VID.getErrorMessage()));
		assertEquals(false,requestValidator.validateVid("123"));
	}

	@Test(expected = InvalidInputException.class)
	public void testEmptyTransactionId(){
		requestValidator.validateTransactionId("");
	}

	@Test(expected = InvalidInputException.class)
	public void testEmptyUserIdAndTransactionId(){
		requestValidator.validateUserIdAndTransactionId("", "3232323232");
	}

	@Test(expected = InvalidInputException.class)
	public void testNullUserIdAndTransactionId(){
		requestValidator.validateUserIdAndTransactionId(null, "3232323232");
	}

	@Test
	public void testValidateTransliterationIdSuccess() throws Exception{
		ReflectionTestUtils.setField(requestValidator, "transliterateId", "mosip.resident.transliteration.transliterate");
		MainRequestDTO<TransliterationRequestDTO> requestDTO = new MainRequestDTO<>();
		TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
		requestDTO.setId("mosip.resident.transliteration.transliterate");
		requestValidator.validateId(requestDTO);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateOnlyLanguageCode(){
		requestValidator.validateOnlyLanguageCode(null);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateOnlyEmptyLanguageCode(){
		requestValidator.validateOnlyLanguageCode("");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateOnlyInvalidLanguageCode(){
		requestValidator.validateOnlyLanguageCode("fra");
	}

	@Test
	public void testValidateOnlyValidLanguageCodeSuccess(){
		requestValidator.validateOnlyLanguageCode("eng");
	}

	@Test
	public void testValidateOnlyInvalidLanguageCodeSuccess(){
		requestValidator.validateOnlyLanguageCode("ara");
	}

	@Test
	public void testValidateEventIdLanguageCodeSuccess(){
		requestValidator.validateEventIdLanguageCode("3434343434","ara");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateEmptyEventIdLanguageCodeSuccess(){
		requestValidator.validateEventIdLanguageCode("","ara");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateSortType(){
		ReflectionTestUtils.invokeMethod(requestValidator, "validateSortType", "D", "sortType");
	}

	@Test
	public void testValidateLocalTime(){
		assertEquals(false,ReflectionTestUtils.invokeMethod(requestValidator, "isValidDate", LocalDateTime.of
				(-1, 4, 4, 4, 4, 4)));
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateStatusFilter(){
		ReflectionTestUtils.invokeMethod(requestValidator, "validateStatusFilter", "", "sortType");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateFromDateTimeToDateTime(){
		requestValidator.validateFromDateTimeToDateTime(null, null, "fromDate");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateFromDateTimeToDateTimeFromDateTimeNull(){
		requestValidator.validateFromDateTimeToDateTime(null, LocalDateTime.MAX, "fromDate");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateFromDateTimeToDateTimeToDateTimeNull(){
		requestValidator.validateFromDateTimeToDateTime(LocalDateTime.MAX, null, "fromDate");
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateFromDateTimeToDateTimeToDateTime(){
		requestValidator.validateFromDateTimeToDateTime(LocalDateTime.MAX, LocalDateTime.MIN, "fromDate");
	}
}
