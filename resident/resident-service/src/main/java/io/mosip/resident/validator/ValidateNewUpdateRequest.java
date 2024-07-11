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
import io.mosip.resident.service.impl.GetPendingDrafts;
import io.mosip.resident.service.impl.GetRemainingUpdateCountByIndividualId;
import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class ValidateNewUpdateRequest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GetRemainingUpdateCountByIndividualId getRemainingUpdateCountByIndividualId;

    @Autowired
    private GetPendingDrafts getPendingDrafts;

    public void validateUpdateCountLimit(Set<String> identity) throws ResidentServiceCheckedException {
        Set<String> attributesHavingLimitExceeded = new HashSet<>();
        if(!identity.isEmpty()) {
            ResponseWrapper<?> responseWrapper =  getRemainingUpdateCountByIndividualId.getRemainingUpdateCountByIndividualId(List.of());
            AttributeListDto attributeListDto = objectMapper.convertValue(responseWrapper.getResponse(), AttributeListDto.class);

            attributesHavingLimitExceeded = attributeListDto.getAttributes().stream()
                    .filter(updateCountDto -> identity.contains(updateCountDto.getAttributeName())
                            && updateCountDto.getNoOfUpdatesLeft() == ResidentConstants.ZERO)
                    .map(UpdateCountDto::getAttributeName)
                    .collect(Collectors.toSet());

        }
        if (!attributesHavingLimitExceeded.isEmpty()) {
            String exceededAttributes = String.join(ResidentConstants.COMMA, attributesHavingLimitExceeded);
            throw new ResidentServiceCheckedException(ResidentErrorCode.UPDATE_COUNT_LIMIT_EXCEEDED.getErrorCode(),
                    String.format(ResidentErrorCode.UPDATE_COUNT_LIMIT_EXCEEDED.getErrorMessage(), exceededAttributes));
        }
    }

    public void validateNewUpdateRequest() throws ResidentServiceCheckedException, ApisResourceAccessException {
        if(Utility.isSecureSession()){
            validatePendingDraft();
        }
    }

    private void validatePendingDraft() throws ResidentServiceCheckedException {
        ResponseWrapper<DraftResidentResponseDto> getPendingDraftResponseDto= getPendingDrafts.getPendingDrafts(null);
        if(!getPendingDraftResponseDto.getResponse().getDrafts().isEmpty()){
            List<DraftUinResidentResponseDto> draftResidentResponseDto = getPendingDraftResponseDto.getResponse().getDrafts();
            for(DraftUinResidentResponseDto uinResidentResponseDto : draftResidentResponseDto){
                if(uinResidentResponseDto.isCancellable()){
                    throw new ResidentServiceCheckedException(ResidentErrorCode.NOT_ALLOWED_TO_UPDATE_UIN_PENDING_PACKET);
                } else {
                    throw new ResidentServiceCheckedException(ResidentErrorCode.NOT_ALLOWED_TO_UPDATE_UIN_PENDING_REQUEST);
                }
            }
        }
    }
}
