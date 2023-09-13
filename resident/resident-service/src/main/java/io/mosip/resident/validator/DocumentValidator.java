package io.mosip.resident.validator;

import static io.mosip.resident.constant.ResidentConstants.ALLOWED_FILE_TYPE;
import static io.mosip.resident.constant.ResidentErrorCode.DOCUMENT_FILE_SIZE;
import static io.mosip.resident.constant.ResidentErrorCode.UN_SUPPORTED_FILE_TYPE;
import static io.mosip.resident.constant.ResidentErrorCode.VIRUS_SCAN_FAILED;
import static io.mosip.resident.constant.ResidentErrorCode.VIRUS_SCAN;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

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

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ProxyMasterdataService;
/**
 * It validates the request and scans the file for viruses
 * 
 * @author Manoj SP
 */
@Component
public class DocumentValidator implements Validator {

	private static final String DOC_TYP_CODE = "docTypCode";

	private static final String DOC_CAT_CODE = "docCatCode";

	private static final Logger logger = LoggerConfiguration.logConfig(DocumentValidator.class);

	@Autowired(required = false)
	private VirusScanner<Boolean, InputStream> virusScanner;

	@Autowired
	private Environment env;

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	@Value("${mosip.max.file.upload.size.in.bytes}")
	private int maxFileUploadSize;

	@Value("${resident.document.validation.transaction-id.regex}")
	private String transactionIdRegex;

	@Value("${resident.document.validation.document-id.regex}")
	private String documentIdRegex;

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
	 * @throws ResidentServiceCheckedException 
	 *
	 */
	public void validateRequest(String transactionId, String docCatCode, String docTypCode, String langCode) throws ResidentServiceCheckedException {
		validateTransactionIdForDocument(transactionId);
		if (docCatCode == null || StringUtils.isEmpty(docCatCode)) {
			throw new InvalidInputException(DOC_CAT_CODE);
		}
		if (docTypCode == null || StringUtils.isEmpty(docTypCode)) {
			throw new InvalidInputException(DOC_TYP_CODE);
		}
		requestValidator.validateLanguageCode(langCode);
		validateDocCatCode(docCatCode, langCode);
		validateDocTypeCode(docCatCode, docTypCode, langCode);
	}

	public void validateDocCatCode(String docCatCode, String langCode) throws ResidentServiceCheckedException {
		List<String> docCatCodeList = proxyMasterdataService.getValidDocCatAndTypeList(langCode).getT1();
		if (!docCatCodeList.contains(docCatCode.toLowerCase())) {
			throw new InvalidInputException(DOC_CAT_CODE);
		}
	}

	public void validateDocTypeCode(String docCatCode, String docTypeCode, String langCode)
			throws ResidentServiceCheckedException {
		List<String> docTypeCodeList = proxyMasterdataService.getValidDocCatAndTypeList(langCode).getT2()
				.get(docCatCode.toLowerCase());
		if (!docTypeCodeList.contains(docTypeCode.toLowerCase())) {
			throw new InvalidInputException(DOC_TYP_CODE);
		}
	}

	/**
	 * It scans the file for viruses
	 * 
	 * @param file The file to be scanned.
	 */
	public void scanForViruses(MultipartFile file) {
		if (env.getProperty(ResidentConstants.VIRUS_SCANNER_ENABLED, Boolean.class, true)) {
			try {
				boolean isInputFileClean = virusScanner.scanFile(file.getInputStream());
				if(!isInputFileClean) {
					throw new ResidentServiceException(VIRUS_SCAN.getErrorCode(),
							VIRUS_SCAN.getErrorMessage());
				}
			} catch (VirusScannerException | IOException e) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(),
						"Virus scan failed - " + ExceptionUtils.getStackTrace(e));
				throw new ResidentServiceException(VIRUS_SCAN_FAILED.getErrorCode(),
						VIRUS_SCAN_FAILED.getErrorMessage());
			}
		}
	}

	public void validateTransactionIdForDocument(String transactionId) {
		if (transactionId == null || StringUtils.isEmpty(transactionId)) {
			throw new InvalidInputException("transactionId");
		} else if (!isDataValidWithRegex(transactionId, transactionIdRegex)) {
			throw new InvalidInputException("transactionId");
		}
	}

	private boolean isDataValidWithRegex(String inputData, String regex) {
		return inputData.matches(regex);
	}

	public void validateDocumentIdAndTransactionId(String documentId, String transactionId) {
		validateTransactionIdForDocument(transactionId);
		validateDocumentId(documentId);
	}

	public void validateDocumentId(String documentId) {
		if (documentId == null || StringUtils.isEmpty(documentId)) {
			throw new InvalidInputException("documentId");
		} else if (!isDataValidWithRegex(documentId, documentIdRegex)) {
			throw new InvalidInputException("documentId");
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
