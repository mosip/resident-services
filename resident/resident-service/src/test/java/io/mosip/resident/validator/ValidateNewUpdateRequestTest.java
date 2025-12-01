package io.mosip.resident.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.AttributeListDto;
import io.mosip.resident.dto.UpdateCountDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;
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
    
}