package io.mosip.resident.validator;

import static io.mosip.resident.constant.ResidentErrorCode.INVALID_INPUT;
import static io.mosip.resident.constant.ResidentErrorCode.VIRUS_SCAN_FAILED;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.DocumentRequestDTO;
import io.mosip.resident.exception.ResidentServiceException;

/**
 * It validates the request and scans the file for viruses
 * 
 * @author Manoj SP
 */
@Component
public class DocumentValidator implements Validator {

	private static final Logger logger = LoggerConfiguration.logConfig(DocumentValidator.class);

	@Autowired(required = false)
	private VirusScanner<Boolean, InputStream> virusScanner;

	@Autowired
	private Environment env;

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(RequestWrapper.class);
	}

	@Override
	public void validate(Object target, Errors errors) {

	}

	/**
	 * This function validates the input parameters of a DocumentRequestDTO object
	 * 
	 * @param docRequest The request object that is passed to the service.
	 */
	public void validateRequest(DocumentRequestDTO docRequest) {
		Objects.requireNonNull(docRequest, String.format(INVALID_INPUT.getErrorMessage() + "request"));
		Objects.requireNonNull(StringUtils.defaultIfBlank(docRequest.getDocCatCode(), null),
				String.format(INVALID_INPUT.getErrorMessage() + "request/docCatCode"));
		Objects.requireNonNull(StringUtils.defaultIfBlank(docRequest.getDocTypCode(), null),
				String.format(INVALID_INPUT.getErrorMessage() + "request/docTypCode"));
		Objects.requireNonNull(StringUtils.defaultIfBlank(docRequest.getLangCode(), null),
				String.format(INVALID_INPUT.getErrorMessage() + "request/langCode"));
	}

	/**
	 * It scans the file for viruses
	 * 
	 * @param file The file to be scanned.
	 */
	public void scanForViruses(MultipartFile file) {
		if (env.getProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, Boolean.class, true)) {
			try {
				virusScanner.scanFile(file.getInputStream());
			} catch (VirusScannerException | IOException e) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						"Virus scan failed - " + ExceptionUtils.getStackTrace(e));
				throw new ResidentServiceException(VIRUS_SCAN_FAILED.getErrorCode(),
						VIRUS_SCAN_FAILED.getErrorMessage());
			}
		}
	}

	public void validateGetDocumentByDocumentIdInput(String transactionId) {
		if(!isNumeric(transactionId)){
			throw new ResidentServiceException(INVALID_INPUT.getErrorCode(),
					INVALID_INPUT.getErrorMessage() + "request/transactionId");
		}
	}
	private boolean isNumeric(String transactionId) {
		return transactionId.matches("[0-9]*");
	}
}
