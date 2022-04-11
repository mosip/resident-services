package io.mosip.resident.service.impl;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;

@Component
public class ProxyMasterdataServiceImpl implements ProxyMasterdataService {

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	Environment env;

	private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

	@Override
	public ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException {
		logger.debug("ProxyMasterdataServiceImpl::getValidDocumentByLangCode():: entry");
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		try {
			String validDocumentGetUrl = env.getProperty(ApiName.VALID_DOCUMENT_BY_LANGCODE_URL.name()) + langCode;
			URI validDocumentGetUri = URI.create(validDocumentGetUrl);
			responseWrapper = (ResponseWrapper<?>) residentServiceRestClient.getApi(validDocumentGetUri,
					ResponseWrapper.class);
			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				logger.debug(responseWrapper.getErrors().get(0).toString());
				throw new ResidentServiceCheckedException(responseWrapper.getErrors().get(0).getErrorCode(),
						responseWrapper.getErrors().get(0).getMessage());
			}
		} catch (ApisResourceAccessException e) {
			auditUtil.setAuditRequestDto(EventEnum.GET_VALID_DOCUMENT_EXCEPTION);
			logger.error("Error occured in accessing valid documents %s", e.getMessage());
			throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ProxyMasterdataServiceImpl::getValidDocumentByLangCode():: exit");
		return responseWrapper;
	}

}
