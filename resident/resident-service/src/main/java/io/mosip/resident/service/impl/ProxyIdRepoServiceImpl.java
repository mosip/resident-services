package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.dto.AttributeListDto;
import io.mosip.resident.dto.UpdateCountDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyIdRepoService;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.mosip.resident.constant.ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION;

/**
 * @author Manoj SP
 *
 */
@Service
public class ProxyIdRepoServiceImpl implements ProxyIdRepoService {

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyIdRepoServiceImpl.class);

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ResidentConfigServiceImpl residentConfigService;

	@Autowired
	private ObjectMapper objectMapper;

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

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				ResponseWrapper<AttributeListDto> listDtoResponseWrapper = new ResponseWrapper<>();
				listDtoResponseWrapper.setResponse(getRemainingUpdateCountFromConfig(attributeList));
				logger.debug("ProxyIdRepoServiceImpl::getRemainingUpdateCountByIndividualId()::exit");
				return listDtoResponseWrapper;
			}
			logger.debug("ProxyIdRepoServiceImpl::getRemainingUpdateCountByIndividualId()::exit");
			return responseWrapper;
			
		} catch (ApisResourceAccessException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private AttributeListDto getRemainingUpdateCountFromConfig(List<String> attributeList) throws ResidentServiceCheckedException, IOException {
		String identityMapping = residentConfigService.getIdentityMapping();
		Map<String, Object> identityMappingMap = objectMapper
				.readValue(identityMapping.getBytes(StandardCharsets.UTF_8), Map.class);
		Object identityObj = identityMappingMap.get(MappingJsonConstants.ATTRIBUTE_UPDATE_COUNT_LIMIT);
		Map<String, Object> attributeUpdateCountLimit = (Map<String, Object>) identityObj;
		AttributeListDto attributeListDto= new AttributeListDto();
		List<UpdateCountDto> updateCountDtoList = new ArrayList<>();
		if(attributeList == null || attributeList.isEmpty()){
			attributeUpdateCountLimit.keySet().forEach(key ->
					addAttributeInUpdateCountDtoList(key, attributeUpdateCountLimit, updateCountDtoList));
		} else {
			attributeList.stream()
					.filter(attributeUpdateCountLimit::containsKey)
					.forEach(attribute -> addAttributeInUpdateCountDtoList(attribute, attributeUpdateCountLimit, updateCountDtoList));
		}
		attributeListDto.setAttributes(updateCountDtoList);
		return attributeListDto;
	}
	private void addAttributeInUpdateCountDtoList(String key, Map<String, Object> attributeUpdateCountLimit, List<UpdateCountDto> updateCountDtoList){
		UpdateCountDto updateCountDto= new UpdateCountDto();
		updateCountDto.setAttributeName(key);
		updateCountDto.setNoOfUpdatesLeft((Integer) attributeUpdateCountLimit.get(key));
		updateCountDtoList.add(updateCountDto);
	}
}
