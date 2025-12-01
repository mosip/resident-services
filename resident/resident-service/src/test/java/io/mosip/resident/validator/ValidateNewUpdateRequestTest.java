package io.mosip.resident.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AttributeListDto;
import io.mosip.resident.dto.DraftResidentResponseDto;
import io.mosip.resident.dto.DraftUinResidentResponseDto;
import io.mosip.resident.dto.UpdateCountDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.IdentityServiceTest;
import io.mosip.resident.service.impl.PendingDrafts;
import io.mosip.resident.service.impl.RemainingUpdateCountByIndividualId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidateNewUpdateRequestTest {

    @InjectMocks
    private ValidateNewUpdateRequest validator;

    @Mock
    private RemainingUpdateCountByIndividualId remainingUpdateCountByIndividualId;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PendingDrafts pendingDrafts;

    @Test
    public void validateUpdateCountLimit_emptyIdentity_shouldNotCallRemainingService()
            throws ResidentServiceCheckedException {

        validator.validateUpdateCountLimit(Collections.emptySet());

        verifyNoInteractions(remainingUpdateCountByIndividualId);
        verifyNoInteractions(objectMapper);
    }

    @Test
    public void validateUpdateCountLimit_attributeHasUpdatesLeft_shouldNotThrow() throws ResidentServiceCheckedException {
        // prepare DTO with updates left > 0
        UpdateCountDto dto = new UpdateCountDto();
        dto.setAttributeName("email");
        dto.setNoOfUpdatesLeft(1);

        AttributeListDto attributeListDto = new AttributeListDto();
        attributeListDto.setAttributes(List.of(dto));
        
        ResponseWrapper responseWrapper = new ResponseWrapper<>();
        responseWrapper.setVersion("v1");
        responseWrapper.setId("1");
        responseWrapper.setResponse(attributeListDto);
        when(remainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(any())).thenReturn(responseWrapper);

        // objectMapper convertValue returns attributeListDto
        when(objectMapper.convertValue(responseWrapper.getResponse(), AttributeListDto.class))
                .thenReturn(attributeListDto);

        // call with identity that contains "email" - should not throw
        Set<String> identity = Set.of("email");
        validator.validateUpdateCountLimit(identity);

        verify(remainingUpdateCountByIndividualId, times(1)).getRemainingUpdateCountByIndividualId(Mockito.<List<String>>any());
        verify(objectMapper, times(1)).convertValue(responseWrapper.getResponse(), AttributeListDto.class);
    }

    @Test
    public void validateUpdateCountLimit_attributeHasZeroUpdates_shouldThrowCheckedException() throws ResidentServiceCheckedException {
        // prepare DTO with zero updates left
        UpdateCountDto dto = new UpdateCountDto();
        dto.setAttributeName("mobile");
        dto.setNoOfUpdatesLeft(ResidentConstants.ZERO);

        AttributeListDto attributeListDto = new AttributeListDto();
        attributeListDto.setAttributes(List.of(dto));

        ResponseWrapper responseWrapper = new ResponseWrapper<>();
        responseWrapper.setVersion("v1");
        responseWrapper.setId("1");
        responseWrapper.setResponse(attributeListDto);
        when(remainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(any())).thenReturn(responseWrapper);

        when(remainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(Mockito.<List<String>>any()))
                .thenReturn(responseWrapper);

        when(objectMapper.convertValue(responseWrapper.getResponse(), AttributeListDto.class))
                .thenReturn(attributeListDto);

        Set<String> identity = Set.of("mobile");

        try {
            validator.validateUpdateCountLimit(identity);
            fail("Expected ResidentServiceCheckedException when attribute has zero updates left");
        } catch (ResidentServiceCheckedException ex) {
            assertEquals(ResidentErrorCode.UPDATE_COUNT_LIMIT_EXCEEDED.getErrorCode(), ex.getErrorCode());
            assertTrue("Error message should contain attribute", ex.getMessage().contains("mobile"));
        }

        verify(remainingUpdateCountByIndividualId, times(1)).getRemainingUpdateCountByIndividualId(Mockito.<List<String>>any());
        verify(objectMapper, times(1)).convertValue(responseWrapper.getResponse(), AttributeListDto.class);
    }

    // ---------- validateNewUpdateRequest tests ----------

    @Test
    public void validateNewUpdateRequest_notSecureSession_shouldNotCallPendingDrafts()
            throws ResidentServiceCheckedException, ApisResourceAccessException {

        // should not throw
        validator.validateNewUpdateRequest();

        verifyNoInteractions(pendingDrafts);
    }

    @Test
    public void validateNewUpdateRequest_secureSession_noPendingDrafts_shouldNotThrow()
            throws ResidentServiceCheckedException, ApisResourceAccessException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();

        DraftResidentResponseDto responseDto = new DraftResidentResponseDto();
        responseDto.setDrafts(Collections.emptyList());

        ResponseWrapper<DraftResidentResponseDto> wrapper = new ResponseWrapper<>();
        wrapper.setResponse(responseDto);

        when(pendingDrafts.getPendingDrafts(null)).thenReturn(wrapper);

        // should not throw
        validator.validateNewUpdateRequest();

        verify(pendingDrafts, times(1)).getPendingDrafts(null);
    }

    @Test
    public void validateNewUpdateRequest_secureSession_pendingDrafts_cancellable_shouldThrowPacketError()
            throws ApisResourceAccessException, ResidentServiceCheckedException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();

        DraftUinResidentResponseDto draft = new DraftUinResidentResponseDto();
        draft.setCancellable(true);

        DraftResidentResponseDto responseDto = new DraftResidentResponseDto();
        responseDto.setDrafts(List.of(draft));
        ResponseWrapper<DraftResidentResponseDto> wrapper = new ResponseWrapper<>();
        wrapper.setResponse(responseDto);

        when(pendingDrafts.getPendingDrafts(null)).thenReturn(wrapper);

        try {
            validator.validateNewUpdateRequest();
            fail("Expected ResidentServiceCheckedException for pending cancelable draft");
        } catch (ResidentServiceCheckedException ex) {
            assertEquals(ResidentErrorCode.NOT_ALLOWED_TO_UPDATE_UIN_PENDING_PACKET.getErrorCode(),
                    ex.getErrorCode());
        }

        verify(pendingDrafts, times(1)).getPendingDrafts(null);
    }

    @Test
    public void validateNewUpdateRequest_secureSession_pendingDrafts_notCancellable_shouldThrowRequestError()
            throws ApisResourceAccessException, ResidentServiceCheckedException {
        IdentityServiceTest.getAuthUserDetailsFromAuthentication();

        DraftUinResidentResponseDto draft = new DraftUinResidentResponseDto();
        draft.setCancellable(false);

        DraftResidentResponseDto responseDto = new DraftResidentResponseDto();
        responseDto.setDrafts(List.of(draft));
        ResponseWrapper<DraftResidentResponseDto> wrapper = new ResponseWrapper<>();
        wrapper.setResponse(responseDto);

        when(pendingDrafts.getPendingDrafts(null)).thenReturn(wrapper);

        try {
            validator.validateNewUpdateRequest();
            fail("Expected ResidentServiceCheckedException for pending non-cancelable draft");
        } catch (ResidentServiceCheckedException ex) {
            assertEquals(ResidentErrorCode.NOT_ALLOWED_TO_UPDATE_UIN_PENDING_REQUEST.getErrorCode(),
                    ex.getErrorCode());
        }

        verify(pendingDrafts, times(1)).getPendingDrafts(null);
    }
    
}