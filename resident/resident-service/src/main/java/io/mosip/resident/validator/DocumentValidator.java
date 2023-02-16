package io.mosip.resident.validator;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static io.mosip.resident.constant.ResidentConstants.ALLOWED_FILE_TYPE;
import static io.mosip.resident.constant.ResidentErrorCode.INVALID_INPUT;
import static io.mosip.resident.constant.ResidentErrorCode.UN_SUPPORTED_FILE_TYPE;
import static io.mosip.resident.constant.ResidentErrorCode.VIRUS_SCAN_FAILED;
import static io.mosip.resident.constant.ResidentErrorCode.DOCUMENT_FILE_SIZE;
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

	@Autowired
	private RequestValidator requestValidator;
	
	@Value("${mosip.max.file.upload.size.in.bytes}")
	private int maxFileUploadSize;

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(RequestWrapper.class);
	}

	@Override
	public void validate(Object target, Errors errors) {

	}

	/**
	 * This function validates the input parameters of a DocumentRequestDTO object
	 * @param langCode 
	 * @param docTypCode 
	 * @param docCatCode 
	 *
	 */
	public void validateRequest(String docCatCode, String docTypCode, String langCode) {

		if (docCatCode == null || StringUtils.isEmpty(docCatCode)) {
			throw new InvalidInputException("docCatCode");
		}
		if (docTypCode == null || StringUtils.isEmpty(docTypCode)) {
			throw new InvalidInputException("docTypCode");
		}
		requestValidator.validateOnlyLanguageCode(langCode);
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

	public void validateTransactionId(String transactionId) {
		if(!isNumeric(transactionId)){
			throw new ResidentServiceException(INVALID_INPUT.getErrorCode(),
					INVALID_INPUT.getErrorMessage() + "transactionId");
		}
	}
	private boolean isNumeric(String transactionId) {
		return transactionId.matches("[0-9]*");
	}

	public void validateDocumentIdAndTransactionId(String documentId, String transactionId) {
		if(!isNumeric(transactionId) && documentId.length() <20){
			throw new ResidentServiceException(INVALID_INPUT.getErrorCode(),
					INVALID_INPUT.getErrorMessage() + "documentId/transactionId");
		} else{
			validateTransactionId(transactionId);
			validateDocumentId(documentId);
		}
	}

	public void validateDocumentId(String documentId) {
		if(documentId == null || documentId.length() < 20){
			throw new ResidentServiceException(INVALID_INPUT.getErrorCode(),
					INVALID_INPUT.getErrorMessage() + "documentId");
		}
	}

	public void validateFileName(MultipartFile file) {
		String extension = Objects.requireNonNull(FilenameUtils.getExtension(file.getOriginalFilename())).toLowerCase();
		String extensionProperty = Objects.requireNonNull(env.getProperty(ALLOWED_FILE_TYPE)).toLowerCase();
		if (!extensionProperty.contains(Objects.requireNonNull(extension))) {
			throw new ResidentServiceException(UN_SUPPORTED_FILE_TYPE.getErrorCode(), UN_SUPPORTED_FILE_TYPE.getErrorMessage());
		}
		if (file.getSize() > maxFileUploadSize) {
			throw new ResidentServiceException(DOCUMENT_FILE_SIZE.getErrorCode(), DOCUMENT_FILE_SIZE.getErrorMessage());
		}
	}
}
