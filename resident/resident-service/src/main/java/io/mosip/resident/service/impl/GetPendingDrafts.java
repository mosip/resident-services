package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.*;
import io.mosip.resident.dto.DraftResidentResponseDto;
import io.mosip.resident.dto.DraftResponseDto;
import io.mosip.resident.dto.DraftUinResidentResponseDto;
import io.mosip.resident.dto.DraftUinResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.util.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;

import java.util.*;

import static io.mosip.resident.constant.ResidentConstants.SEMI_COLON;
import static io.mosip.resident.constant.ResidentConstants.UPDATED;
import static io.mosip.resident.constant.ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION;

@Component
public class GetPendingDrafts {

	private static final Logger logger = LoggerConfiguration.logConfig(GetPendingDrafts.class);

	@Autowired
	private AvailableClaimUtility availableClaimUtility;

	@Autowired
	private UinVidValidator uinVidValidator;

	@Autowired
	private UinForIndividualId uinForIndividualId;

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	private static final String INVALID_INPUT_PARAMETER_ID_REPO_ERROR_CODE = "IDR-IDC-002";

	@Autowired
	private Environment environment;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	private Utility utility;

	@Autowired
	private MaskDataUtility maskDataUtility;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private GetEventStatusCode getEventStatusCode;

	@Autowired
	private GetDescriptionForLangCode getDescriptionForLangCode;

	public ResponseWrapper<DraftResidentResponseDto> getPendingDrafts(String langCode) throws ResidentServiceCheckedException {
		try {
			logger.debug("ProxyIdRepoServiceImpl::getPendingDrafts()::entry");
			String individualId= availableClaimUtility.getResidentIndvidualIdFromSession();
			if(!uinVidValidator.validateUin(individualId)){
				individualId = uinForIndividualId.getUinForIndividualId(individualId);
			}
			Map<String, Object> pathsegements = new HashMap<String, Object>();
			pathsegements.put(IdType.UIN.name(), individualId);

			ResponseWrapper<DraftResponseDto> responseWrapper = residentServiceRestClient.getApi(ApiName.IDREPO_IDENTITY_GET_DRAFT_UIN,
					pathsegements, ResponseWrapper.class);
			ResponseWrapper<DraftResidentResponseDto> responseWrapperResident = new ResponseWrapper<>();
			responseWrapperResident.setId(environment.getProperty(ResidentConstants.GET_PENDING_DRAFT_ID, ResidentConstants.GET_PENDING_DRAFT_ID));
			responseWrapperResident.setVersion(environment.getProperty(ResidentConstants.GET_PENDING_DRAFT_VERSION,
					ResidentConstants.GET_PENDING_DRAFT_VERSION_DEFAULT_VALUE));
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()){
				if(responseWrapper.getErrors().get(ResidentConstants.ZERO) != null && !responseWrapper.getErrors().get(ResidentConstants.ZERO).toString().isEmpty() &&
						responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode() != null &&
						!responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode().isEmpty() &&
						responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode().equalsIgnoreCase(INVALID_INPUT_PARAMETER_ID_REPO_ERROR_CODE)) {
					throw new InvalidInputException(IdType.UIN.name());
				}else {
					throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
				}
			} else {
				DraftResponseDto draftResponseDto = objectMapper.convertValue(responseWrapper.getResponse(), DraftResponseDto.class);
				responseWrapperResident.setResponse(convertDraftResponseDtoToResidentResponseDTo(draftResponseDto, individualId, langCode));
			}
			logger.debug("ProxyIdRepoServiceImpl::getPendingDrafts()::exit");
			return responseWrapperResident;

		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	private DraftResidentResponseDto convertDraftResponseDtoToResidentResponseDTo(DraftResponseDto response, String individualId, String langCode) throws ResidentServiceCheckedException, ApisResourceAccessException {
		List<DraftUinResponseDto> draftsList = response.getDrafts();
		List<DraftUinResidentResponseDto> draftUinResidentResponseDtos = new ArrayList<>();
		DraftResidentResponseDto draftResidentResponseDto = new DraftResidentResponseDto();
		Set<String> eventIdList = new HashSet<>();
		if(draftsList!=null && !draftsList.isEmpty()) {
			for (DraftUinResponseDto draftUinResponseDto : draftsList) {
				String eventId = setDraftValue(draftUinResponseDto.getRid(), individualId, draftUinResponseDto.getAttributes(),
						null, draftUinResponseDto.getCreatedDTimes(), draftUinResidentResponseDtos, true, langCode, null);
				eventIdList.add(eventId);
			}
		}
		List<ResidentTransactionEntity> residentTransactionEntityList = residentTransactionRepository.
				findByTokenIdAndRequestTypeCodeAndStatusCode(availableClaimUtility.getResidentIdaToken(), RequestType.UPDATE_MY_UIN.name(),
						EventStatusInProgress.NEW.name());
		if(!residentTransactionEntityList.isEmpty()){
			for(ResidentTransactionEntity residentTransactionEntity:residentTransactionEntityList){
				if(!eventIdList.contains(residentTransactionEntity.getEventId()) && residentTransactionEntity.getAttributeList()!=null) {
					setDraftValue(residentTransactionEntity.getAid(), individualId,
							List.of(residentTransactionEntity.getAttributeList().split(ResidentConstants.COMMA)),
							residentTransactionEntity.getEventId(), residentTransactionEntity.getCrDtimes().toString(),
							draftUinResidentResponseDtos, false, langCode, residentTransactionEntity);
				}
			}
		}
		draftResidentResponseDto.setDrafts(draftUinResidentResponseDtos);
		return draftResidentResponseDto;
	}

	private String setDraftValue(String rid, String individualId, List<String> attributeList, String eventId, String createdDtimes,
								 List<DraftUinResidentResponseDto> draftUinResidentResponseDtos, boolean cancellableStatus,
								 String langCode, ResidentTransactionEntity residentTransactionEntity) throws ResidentServiceCheckedException,
			ApisResourceAccessException {
		DraftUinResidentResponseDto draftUinResidentResponseDto = new DraftUinResidentResponseDto();
		if(residentTransactionEntity==null){
			residentTransactionEntity = getEventIdFromRid(rid, individualId, attributeList);
		}
		if (eventId == null) {
			eventId = residentTransactionEntity.getEventId();
		}
		draftUinResidentResponseDto.setEid(eventId);
		draftUinResidentResponseDto.setAid(rid);
		draftUinResidentResponseDto.setAttributes(attributeList);
		draftUinResidentResponseDto.setCreatedDTimes(createdDtimes);
		draftUinResidentResponseDto.setCancellable(cancellableStatus);
		draftUinResidentResponseDto.setDescription(getDescription(residentTransactionEntity, langCode));
		draftUinResidentResponseDtos.add(draftUinResidentResponseDto);

		return eventId;
	}

	private String getDescription(ResidentTransactionEntity residentTransactionEntity, String langCode) throws ResidentServiceCheckedException {
		if(langCode == null){
			return "";
		}
		Tuple2<String, String> statusCodes = getEventStatusCode.getEventStatusCode(residentTransactionEntity.getStatusCode(), langCode);
		return getDescriptionForLangCode.getDescriptionForLangCode(residentTransactionEntity, langCode, statusCodes.getT1(),
				RequestType.valueOf(residentTransactionEntity.getRequestTypeCode()));
	}

	private ResidentTransactionEntity getEventIdFromRid(String rid, String individualId, List<String> attributes) throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResidentTransactionEntity residentTransactionEntityAlreadyPresent = residentTransactionRepository.findTopByAidOrderByCrDtimesDesc(rid);
		String eventId = residentTransactionEntityAlreadyPresent.getEventId();
		if(eventId == null){
			ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.UPDATE_MY_UIN);
			eventId = utility.createEventId();
			residentTransactionEntity.setEventId(eventId);
			residentTransactionEntity.setRefId(maskDataUtility.convertToMaskData(individualId));
			residentTransactionEntity.setIndividualId(individualId);
			residentTransactionEntity.setTokenId(availableClaimUtility.getResidentIdaToken());
			residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
			if(attributes!=null){
				String attributeList = String.join(SEMI_COLON, attributes);
				residentTransactionEntity.setAttributeList(attributeList);
				residentTransactionEntity.setStatusComment(attributeList+UPDATED);
			}
			residentTransactionEntity.setConsent(ConsentStatusType.ACCEPTED.name());
			residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
			residentTransactionEntity.setAid(rid);
			residentTransactionEntity.setCredentialRequestId(rid + utility.getRidDeliMeterValue());
			residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
			residentTransactionEntity.setRequestSummary(EventStatusInProgress.NEW.name());
			residentTransactionRepository.save(residentTransactionEntity);
			return residentTransactionEntity;
		}
		return residentTransactionEntityAlreadyPresent;
	}
}
