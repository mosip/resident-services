package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idrepository.core.dto.IdResponseDTO;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ConsentStatusType;
import io.mosip.resident.constant.EventStatusCanceled;
import io.mosip.resident.constant.EventStatusInProgress;
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
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	public static final String NO_RECORDS_FOUND_ID_REPO_ERROR_CODE = "IDR-IDC-007";
	private static final String DISCARDED = "DISCARDED";

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	private Utility utility;

	@Autowired
	private ResidentServiceImpl residentService;

	@Autowired
	private IdentityDataUtil identityDataUtil;

	@Autowired
	private AvailableClaimUtility availableClaimUtility;

	@Autowired
	private MaskDataUtility maskDataUtility;

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
					identityDataUtil.sendNotification(residentTransactionEntity.get().getEventId(),
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

}
