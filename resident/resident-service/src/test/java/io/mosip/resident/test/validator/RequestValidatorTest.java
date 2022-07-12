package io.mosip.resident.test.validator;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.dto.*;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.RequestValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

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
		ReflectionTestUtils.setField(requestValidator, "map", map);

		Mockito.when(uinValidator.validateId(Mockito.anyString())).thenReturn(true);
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(true);
		Mockito.when(ridValidator.validateId(Mockito.anyString())).thenReturn(true);

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

	@Test(expected = InvalidInputException.class)
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
	public void testValidateAuthLockOrUnlockRequestV2InvalidIndividualId() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		authLockOrUnLockRequestDtoV2.setIndividualId(null);
		authLockOrUnLockRequestDtoV2.setAuthType(null);
		List<AuthTypeStatusDto> authTypes = new ArrayList<>();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2InvalidIndividualIdBadIdType() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
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
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
		authLockOrUnLockRequestDtoV2.setAuthType(null);
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
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
		List<AuthTypeStatusDto> authTypes = new ArrayList<>();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("dummy");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test
	public void testValidateAuthLockOrUnlockRequestV2() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
		List<AuthTypeStatusDto> authTypes = new ArrayList<>();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(10L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2EmptyAuthType() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
		List<AuthTypeStatusDto> authTypes = new ArrayList<>();
		authLockOrUnLockRequestDtoV2.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2NullUnlockForSeconds() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
		List<AuthTypeStatusDto> authTypes = new ArrayList<>();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(null);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDtoV2> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString(pattern));
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockOrUnLockRequestDtoV2);
		requestValidator.validateAuthLockOrUnlockRequestV2(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateAuthLockOrUnlockRequestV2EmptyAuthTypeV2() throws Exception{
		AuthLockOrUnLockRequestDtoV2 authLockOrUnLockRequestDtoV2 = new AuthLockOrUnLockRequestDtoV2();
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
		List<AuthTypeStatusDto> authTypes = new ArrayList<>();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypes);
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
		authLockOrUnLockRequestDtoV2.setIndividualId("12344567");
		List<AuthTypeStatusDto> authTypes = new ArrayList<>();
		AuthTypeStatusDto authTypeStatusDto = new AuthTypeStatusDto();
		authTypeStatusDto.setAuthType("bio-FIR");
		authTypeStatusDto.setLocked(true);
		authTypeStatusDto.setUnlockForSeconds(-10L);
		authTypes.add(authTypeStatusDto);
		authLockOrUnLockRequestDtoV2.setAuthType(authTypes);
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
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateServiceHistoryRequestBadServiceType() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "";
		String serviceType = "BadServiceType";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateServiceHistoryRequestBadSortType() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "BadSortType";
		String serviceType = "DATA_SHARE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test(expected = InvalidInputException.class)
	public void testValidateServiceHistoryRequestNullSortType() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String serviceType = "DATA_SHARE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, null, serviceType);
	}

	@Test
	public void testValidateServiceHistoryRequestDateCheck() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "DATA_SHARE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryServiceRequest() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "SERVICE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryID_MANAGEMENT_REQUEST() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "ID_MANAGEMENT_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryDATA_UPDATE_REQUEST() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "DATA_UPDATE_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistoryAUTHENTICATION_REQUEST() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "ASC";
		String serviceType = "AUTHENTICATION_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
	}

	@Test
	public void testValidateServiceHistoryRequestServiceHistorySuccess() throws Exception{
		LocalDateTime fromDate = LocalDateTime.now();
		LocalDateTime toDate = LocalDateTime.now();
		String sortType = "DESC";
		String serviceType = "AUTHENTICATION_REQUEST";
		requestValidator.validateServiceHistoryRequest(fromDate, toDate, sortType, serviceType);
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
}
