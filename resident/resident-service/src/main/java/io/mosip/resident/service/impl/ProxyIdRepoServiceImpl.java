package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idrepository.core.dto.IdResponseDTO;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ConsentStatusType;
import io.mosip.resident.constant.EventStatusCanceled;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.DraftResidentResponseDto;
import io.mosip.resident.dto.DraftResponseDto;
import io.mosip.resident.dto.DraftUinResidentResponseDto;
import io.mosip.resident.dto.DraftUinResponseDto;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.mosip.resident.constant.ResidentConstants.SEMI_COLON;
import static io.mosip.resident.constant.ResidentConstants.UPDATED;
import static io.mosip.resident.constant.ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION;

/**
 * @author Manoj SP
 *
 */
@Service
public class ProxyIdRepoServiceImpl implements ProxyIdRepoService {

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyIdRepoServiceImpl.class);
	private static final String NO_RECORDS_FOUND_ID_REPO_ERROR_CODE = "IDR-IDC-007";
	private static final String INVALID_INPUT_PARAMETER_ID_REPO_ERROR_CODE = "IDR-IDC-002";
	private static final String DISCARDED = "DISCARDED";

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	private Environment environment;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	private Utility utility;

	@Override
	public ResponseWrapper<?> getRemainingUpdateCountByIndividualId(List<String> attributeList)
			throws ResidentServiceCheckedException {
		try {
			logger.debug("ProxyIdRepoServiceImpl::getRemainingUpdateCountByIndividualId()::entry");
			String individualId=identityServiceImpl.getResidentIndvidualIdFromSession();
			Map<String, Object> pathsegements = new HashMap<String, Object>();
			pathsegements.put("individualId", individualId);
			
			List<String> queryParamName = new ArrayList<String>();
			queryParamName.add("attribute_list");

			List<Object> queryParamValue = new ArrayList<>();
			queryParamValue.add(Objects.isNull(attributeList) ? "" : attributeList.stream().collect(Collectors.joining(",")));
			
			ResponseWrapper<?> responseWrapper = residentServiceRestClient.getApi(ApiName.IDREPO_IDENTITY_UPDATE_COUNT,
					pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()){
				if(responseWrapper.getErrors().get(ResidentConstants.ZERO) != null && !responseWrapper.getErrors().get(ResidentConstants.ZERO).toString().isEmpty() &&
						responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode() != null &&
						!responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode().isEmpty() &&
						responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode().equalsIgnoreCase(NO_RECORDS_FOUND_ID_REPO_ERROR_CODE)) {
					throw new ResidentServiceCheckedException(ResidentErrorCode.NO_RECORDS_FOUND);
				}else {
					throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
				}
			}
			logger.debug("ProxyIdRepoServiceImpl::getRemainingUpdateCountByIndividualId()::exit");
			return responseWrapper;
			
		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public ResponseWrapper<DraftResidentResponseDto> getPendingDrafts() throws ResidentServiceCheckedException {
		try {
			logger.debug("ProxyIdRepoServiceImpl::getPendingDrafts()::entry");
			String individualId=identityServiceImpl.getResidentIndvidualIdFromSession();
			if(!requestValidator.validateUin(individualId)){
				individualId = identityServiceImpl.getUinForIndividualId(individualId);
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
				responseWrapperResident.setResponse(convertDraftResponseDtoToResidentResponseDTo(draftResponseDto, individualId));
			}
			logger.debug("ProxyIdRepoServiceImpl::getPendingDrafts()::exit");
			return responseWrapperResident;

		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public String discardDraft(String eid) throws ResidentServiceCheckedException{
		try {
			logger.debug("ProxyIdRepoServiceImpl::discardDraft()::entry");
			List<String> pathsegments = new ArrayList<String>();
			Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository.findById(eid);
			String aid = null;
			String individualId = null;
			if(residentTransactionEntity.isPresent()){
				aid = residentTransactionEntity.get().getAid();
				individualId = residentTransactionEntity.get().getIndividualId();
				if(aid==null){
					throw new ResidentServiceCheckedException(ResidentErrorCode.AID_NOT_FOUND);
				}
			}
			pathsegments.add(aid);

			IdResponseDTO response = (IdResponseDTO) residentServiceRestClient.
					deleteApi(ApiName.IDREPO_IDENTITY_DISCARD_DRAFT, pathsegments, "", "", IdResponseDTO.class);

			if (response.getErrors() != null && !response.getErrors().isEmpty()){
				if(response.getErrors().get(ResidentConstants.ZERO) != null && !response.getErrors().get(ResidentConstants.ZERO).toString().isEmpty() &&
						response.getErrors().get(ResidentConstants.ZERO).getErrorCode() != null &&
						!response.getErrors().get(ResidentConstants.ZERO).getErrorCode().isEmpty() &&
						response.getErrors().get(ResidentConstants.ZERO).getErrorCode().equalsIgnoreCase(NO_RECORDS_FOUND_ID_REPO_ERROR_CODE)) {
					throw new ResidentServiceCheckedException(ResidentErrorCode.NO_RECORDS_FOUND);
				}else {
					throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
				}
			}
			if(response.getResponse().getStatus().equalsIgnoreCase(DISCARDED)){
				if(residentTransactionEntity.isPresent()) {
					utility.updateEntity(EventStatusCanceled.CANCELED.name(), RequestType.UPDATE_MY_UIN.name()
									+ " - " + EventStatusCanceled.CANCELED.name(),
							false, "Draft Discarded successfully", residentTransactionEntity.get());
					utility.sendNotification(residentTransactionEntity.get().getEventId(),
							individualId, TemplateType.REGPROC_FAILED);
				}
			}

			logger.debug("ProxyIdRepoServiceImpl::discardDraft()::exit");
			return response.getResponse().getStatus();

		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
	}

	private DraftResidentResponseDto convertDraftResponseDtoToResidentResponseDTo(DraftResponseDto response, String individualId) throws ResidentServiceCheckedException, ApisResourceAccessException {
		List<DraftUinResponseDto> draftsList = response.getDrafts();
		List<DraftUinResidentResponseDto> draftUinResidentResponseDtos = new ArrayList<>();
		DraftResidentResponseDto draftResidentResponseDto = new DraftResidentResponseDto();
		Set<String> eventIdList = new HashSet<>();
		if(draftsList!=null && !draftsList.isEmpty()) {
			for (DraftUinResponseDto draftUinResponseDto : draftsList) {
				String eventId = setDraftValue(draftUinResponseDto.getRid(), individualId, draftUinResponseDto.getAttributes(),
						null, draftUinResponseDto.getCreatedDTimes(), draftUinResidentResponseDtos, true);
				eventIdList.add(eventId);
			}
		}
		List<ResidentTransactionEntity> residentTransactionEntityList = residentTransactionRepository.
				findByTokenIdAndRequestTypeCodeAndStatusCode(identityServiceImpl.getResidentIdaToken(), RequestType.UPDATE_MY_UIN.name(),
						EventStatusInProgress.NEW.name());
		if(!residentTransactionEntityList.isEmpty()){
			for(ResidentTransactionEntity residentTransactionEntity:residentTransactionEntityList){
				if(!eventIdList.contains(residentTransactionEntity.getEventId())) {
					setDraftValue(residentTransactionEntity.getAid(), individualId,
							List.of(residentTransactionEntity.getAttributeList().split(ResidentConstants.COMMA)),
							residentTransactionEntity.getEventId(), residentTransactionEntity.getCrDtimes().toString(),
							draftUinResidentResponseDtos, false);
				}
			}
		}
		draftResidentResponseDto.setDrafts(draftUinResidentResponseDtos);
		return draftResidentResponseDto;
	}

	private String setDraftValue(String rid, String individualId, List<String> attributeList, String eventId, String createdDtimes,
							   List<DraftUinResidentResponseDto> draftUinResidentResponseDtos, boolean cancellableStatus) throws ResidentServiceCheckedException,
			ApisResourceAccessException {
		DraftUinResidentResponseDto draftUinResidentResponseDto = new DraftUinResidentResponseDto();
		if (eventId == null) {
			eventId = getEventIdFromRid(rid, individualId, attributeList);
		}
		draftUinResidentResponseDto.setEid(eventId);
		draftUinResidentResponseDto.setAid(rid);
		draftUinResidentResponseDto.setAttributes(attributeList);
		draftUinResidentResponseDto.setCreatedDTimes(createdDtimes);
		draftUinResidentResponseDto.setCancellable(cancellableStatus);
		draftUinResidentResponseDtos.add(draftUinResidentResponseDto);

		return eventId;
	}

	private String getEventIdFromRid(String rid, String individualId, List<String> attributes) throws ResidentServiceCheckedException, ApisResourceAccessException {
		String eventId = residentTransactionRepository.findEventIdByAid(rid);
		if(eventId == null){
			ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.UPDATE_MY_UIN);
			eventId = utility.createEventId();
			residentTransactionEntity.setEventId(eventId);
			residentTransactionEntity.setRefId(utility.convertToMaskData(individualId));
			residentTransactionEntity.setIndividualId(individualId);
			residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
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
		}
		return eventId;
	}

}
