package io.mosip.resident.validator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.AuthTypeStatus;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestIdType;
import io.mosip.resident.dto.AuthHistoryRequestDTO;
import io.mosip.resident.dto.AuthLockOrUnLockRequestDto;
import io.mosip.resident.dto.EuinRequestDTO;
import io.mosip.resident.dto.RequestDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentReprintRequestDto;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.exception.InvalidInputException;

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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidUnlockId() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.UNLOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValideuinId() throws Exception {
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authlock");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthHistoryId() throws Exception {
		AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authHistoryRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authlock");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidVersion() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthHistoryVersion() throws Exception {
		AuthHistoryRequestDTO authHistoryRequestDTO = new AuthHistoryRequestDTO();
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authHistoryRequestDTO);
		requestWrapper.setVersion("v2");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValideuinVersion() throws Exception {
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v2");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidRequest() throws Exception {

		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(null);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthHistoryRequest() throws Exception {

		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestWrapper.setRequest(null);
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValideuinRequest() throws Exception {

		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestWrapper.setRequest(null);
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidIndividualType() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualIdType(IdType.RID.name());
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		authLockRequestDto.setIndividualIdType(IdType.VID.name());
		authLockRequestDto.setIndividualId("12345");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testeuinValidIndividualType() throws Exception {
		EuinRequestDTO euinRequestDTO = new EuinRequestDTO();
		euinRequestDTO.setIndividualIdType(IdType.RID.name());
		RequestWrapper<EuinRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidIndividualType() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.RID.name());
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidOtp() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualIdType(IdType.UIN.name());
		authLockRequestDto.setIndividualId("12344567");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidAuthTypes() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualIdType(IdType.UIN.name());
		authLockRequestDto.setOtp("1232354");
		authLockRequestDto.setIndividualId("12344567");
		List<String> authTypes = new ArrayList<String>();
		authTypes.add("bio-FMR");
		authLockRequestDto.setAuthType(authTypes);
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidEmptyAuthTypes() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setTransactionID("12345");
		authLockRequestDto.setIndividualIdType(IdType.UIN.name());
		authLockRequestDto.setOtp("1232354");
		authLockRequestDto.setIndividualId("12344567");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidPageFetch() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.VID.name());
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageStart("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidPageStart() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.VID.name());
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidIndividualId() throws Exception {
		Mockito.when(uinValidator.validateId(Mockito.anyString())).thenReturn(false);
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.UIN.name());
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidpageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.UIN.name());
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1Q");
		authRequestDTO.setPageStart("1");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidpageStart() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.UIN.name());
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("1");
		authRequestDTO.setPageStart("1Q");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidpageStartPageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.UIN.name());
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch(" ");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		authRequestDTO.setPageStart(" ");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
	
	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryinValidpageStartPageFetch() throws Exception {
		
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.UIN.name());
		authRequestDTO.setIndividualId("123");
		authRequestDTO.setPageFetch("-10");
		authRequestDTO.setPageStart("-11");
		authRequestDTO.setOtp("12345");
		authRequestDTO.setTransactionID("12345");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		reqWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		reqWrapper.setRequest(request);
		reqWrapper.setId("mosip.resident.print");
		reqWrapper.setVersion("v1");
		boolean result = requestValidator.validateRequest(reqWrapper, RequestIdType.RE_PRINT_ID);
		assertTrue(result);
	}

	@Test(expected = InvalidInputException.class)
	public void testvalidateRequestInValidId() {

		RequestWrapper<ResidentReprintRequestDto> reqWrapper = new RequestWrapper<>();
		reqWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());

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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(euinRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.euin");
		requestValidator.validateEuinRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidTransactionId() throws Exception {
		AuthLockOrUnLockRequestDto authLockRequestDto = new AuthLockOrUnLockRequestDto();
		authLockRequestDto.setIndividualIdType(IdType.UIN.name());
		authLockRequestDto.setIndividualId("12344567");
		authLockRequestDto.setOtp("12345");
		RequestWrapper<AuthLockOrUnLockRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.authlock");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(authLockRequestDto);
		requestValidator.validateAuthLockOrUnlockRequest(requestWrapper, AuthTypeStatus.LOCK);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidGetStatusId() throws Exception {
		RequestDTO requestDTO = new RequestDTO();
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRequestDTO(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testGetStatusValidIndividualType() throws Exception {
		RequestDTO requestDTO = new RequestDTO();
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();

		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRequestDTO(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testGetStatusValidIndividualId() throws Exception {
		Mockito.when(ridValidator.validateId(Mockito.anyString())).thenReturn(false);
		RequestDTO requestDTO = new RequestDTO();
		RequestWrapper<RequestDTO> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("RID");
		requestDTO.setIndividualId("123456");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.checkstatus");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateRequestDTO(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidUpdateId() throws Exception {
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidIndividualType() throws Exception {
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();

		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidIndividualId() throws Exception {
		Mockito.when(vidValidator.validateId(Mockito.anyString())).thenReturn(false);
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidTransaction() throws Exception {

		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestDTO.setOtp("1234567");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testUpdateValidOtp() throws Exception {

		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestDTO.setIndividualIdType("VID");
		requestDTO.setIndividualId("1234567");
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper);

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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.updateuin");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testValidReprintId() throws Exception {
		ResidentUpdateRequestDto requestDTO = new ResidentUpdateRequestDto();
		RequestWrapper<ResidentUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateUpdateRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testReprintValidIndividualType() throws Exception {
		ResidentReprintRequestDto requestDTO = new ResidentReprintRequestDto();
		RequestWrapper<ResidentReprintRequestDto> requestWrapper = new RequestWrapper<>();

		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
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
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setId("mosip.resident.print");
		requestWrapper.setVersion("v1");
		requestWrapper.setRequest(requestDTO);
		requestValidator.validateReprintRequest(requestWrapper);
	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidOtp() throws Exception
	{
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.VID.name());
		authRequestDTO.setIndividualId("1234567");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}

	@Test(expected = InvalidInputException.class)
	public void testAuthHistoryValidTransactionId() throws Exception {
		AuthHistoryRequestDTO authRequestDTO = new AuthHistoryRequestDTO();
		authRequestDTO.setIndividualIdType(IdType.VID.name());
		authRequestDTO.setIndividualId("1234567");
		authRequestDTO.setOtp("1245");
		RequestWrapper<AuthHistoryRequestDTO> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
		requestWrapper.setRequest(authRequestDTO);
		requestWrapper.setVersion("v1");
		requestWrapper.setId("mosip.resident.authhistory");
		requestValidator.validateAuthHistoryRequest(requestWrapper);

	}
}
