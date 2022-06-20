package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentErrorCode.INVALID_INPUT;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.DocumentRequestDTO;
import io.mosip.resident.dto.DocumentResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.validator.DocumentValidator;

/**
 * The above class is a controller class which is used to upload documents and
 * fetch documents by transaction id
 * 
 * @author Manoj SP
 */
@RestController
public class DocumentController {

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentServiceImpl.class);

	@Autowired
	private DocumentValidator validator;

	@Autowired
	private DocumentService service;
	
	@Autowired
	private AuditUtil audit;

	/**
	 * This function uploads a document to a transaction.
	 * 
	 * @param transactionId String
	 * @param file          The file to be uploaded
	 * @param request       DocumentRequestDTO
	 * @return ResponseWrapper<DocumentResponseDTO>
	 */
	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getPostUploadDocuments()"
		+ ")")
	@PostMapping(path = "/documents/{transaction-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseWrapper<DocumentResponseDTO> uploadDocuments(@PathVariable("transaction-id") String transactionId,
			@RequestPart(value = "file", required = true) MultipartFile file,
			@RequestPart(value = "request", required = true) String request) {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Document Upload API"));
		ResponseWrapper<DocumentResponseDTO> responseWrapper = new ResponseWrapper<>();
		try {
			Objects.requireNonNull(StringUtils.defaultIfBlank(request, null),
					String.format(INVALID_INPUT.getErrorMessage() + "request"));
			DocumentRequestDTO docRequest = JsonUtil
					.readValue(new String(CryptoUtil.decodeURLSafeBase64(request)), new TypeReference<RequestWrapper<DocumentRequestDTO>>() {
					}).getRequest();
			validator.validateRequest(docRequest);
			validator.scanForViruses(file);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.UPLOAD_DOCUMENT, transactionId));
			DocumentResponseDTO uploadDocumentResponse = service.uploadDocument(transactionId, file, docRequest);
			responseWrapper.setResponse(uploadDocumentResponse);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.UPLOAD_DOCUMENT_SUCCESS, transactionId));
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.UPLOAD_DOCUMENT_FAILED, transactionId));
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
		} catch (IOException | NullPointerException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.UPLOAD_DOCUMENT_FAILED, transactionId));
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			responseWrapper.setErrors(List.of(new ServiceError(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
					ResidentErrorCode.BAD_REQUEST.getErrorMessage())));
		}
		return responseWrapper;
	}

	/**
	 * It fetches all documents metadata for a given transaction id
	 * 
	 * @param transactionId The transaction ID of the document
	 * @return ResponseWrapper<List<DocumentResponseDTO>>
	 */
	@PreAuthorize("@scopeValidator.hasAllScopes("
			+ "@authorizedScopes.getGetUploadedDocuments()"
		+ ")")
	@GetMapping(path = "/documents/{transaction-id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseWrapper<List<DocumentResponseDTO>> getDocumentsByTransactionId(
			@PathVariable("transaction-id") String transactionId) {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Get documents API"));
		ResponseWrapper<List<DocumentResponseDTO>> responseWrapper = new ResponseWrapper<>();
		try {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.GET_DOCUMENTS_METADATA, transactionId));
			List<DocumentResponseDTO> documentResponse = service.fetchAllDocumentsMetadata(transactionId);
			responseWrapper.setResponse(documentResponse);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.GET_DOCUMENTS_METADATA_SUCCESS, transactionId));
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.GET_DOCUMENTS_METADATA_FAILED, transactionId));
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
		}
		return responseWrapper;
	}
}
