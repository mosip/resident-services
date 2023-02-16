package io.mosip.resident.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.dto.DocumentDTO;
import io.mosip.resident.dto.DocumentRequestDTO;
import io.mosip.resident.dto.DocumentResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
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
	
	@Value("${resident.document.upload.id}")
	private String residentUploadDocumentId;
	
	@Value("${mosip.resident.request.response.version}")
	private String residentDocumentResponseVersion;

	@Value("${resident.document.get.id}")
	private String residentGetDocumentId;

	@Value("${resident.document.get.version}")
	private String residentGetDocumentVersion;

	@Value("${resident.document.list.id}")
	private String residentDocumentListId;

	@Value("${resident.document.list.version}")
	private String residentDocumentListVersion;

	@Value("${resident.document.delete.id}")
	private String residentDeleteId;

	@Value("${resident.document.delete.version}")
	private String residentDeleteVersion;

	/**
	 * This function uploads a document to a transaction.
	 * 
	 * @param transactionId String
	 * @param file          The file to be uploaded
	 * @return ResponseWrapper<DocumentResponseDTO>
	 */
	@PostMapping(path = "/documents/{transaction-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseWrapper<DocumentResponseDTO> uploadDocuments(@PathVariable("transaction-id") String transactionId,
			@RequestPart(value = "file", required = true) MultipartFile file,
			@RequestParam("docCatCode") String docCatCode,
			@RequestParam("docTypCode") String docTypCode,
			@RequestParam("langCode") String langCode,
			@RequestParam("referenceId") String referenceId) throws IOException {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Document Upload API"));
		ResponseWrapper<DocumentResponseDTO> responseWrapper = new ResponseWrapper<>();
		try {
			validator.validateRequest(docCatCode,docTypCode,langCode);
			validator.validateFileName(file);
			validator.scanForViruses(file);
			DocumentRequestDTO docRequest = new DocumentRequestDTO();
			docRequest.setDocCatCode(docCatCode.toLowerCase());
			docRequest.setDocTypCode(docTypCode);
			docRequest.setLangCode(langCode);
			docRequest.setReferenceId(referenceId);

			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.UPLOAD_DOCUMENT, transactionId));
			DocumentResponseDTO uploadDocumentResponse = service.uploadDocument(transactionId, file, docRequest);
			responseWrapper.setId(residentUploadDocumentId);
			responseWrapper.setVersion(residentDocumentResponseVersion);
			responseWrapper.setResponse(uploadDocumentResponse);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.UPLOAD_DOCUMENT_SUCCESS, transactionId));
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.UPLOAD_DOCUMENT_FAILED, transactionId));
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			responseWrapper.setId(residentUploadDocumentId);
			responseWrapper.setVersion(residentDocumentResponseVersion);
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
		} 
		return responseWrapper;
	}

	/**
	 * It fetches all documents metadata for a given transaction id
	 * 
	 * @param transactionId The transaction ID of the document
	 * @return ResponseWrapper<List<DocumentResponseDTO>>
	 */
	@GetMapping(path = "/documents/{transaction-id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseWrapper<List<DocumentResponseDTO>> getDocumentsByTransactionId(
			@PathVariable("transaction-id") String transactionId) {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Get documents API"));
		ResponseWrapper<List<DocumentResponseDTO>> responseWrapper = new ResponseWrapper<>();
		try {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.GET_DOCUMENTS_METADATA, transactionId));
			validator.validateTransactionId(transactionId);
			List<DocumentResponseDTO> documentResponse = service.fetchAllDocumentsMetadata(transactionId);
			responseWrapper.setId(residentDocumentListId);
			responseWrapper.setVersion(residentDocumentListVersion);
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

	/**
	 * It fetches document for a given document id
	 *
	 * @param transactionId The transaction ID of the document should be passed as Request Param
	 * @param documentId    The document ID of the document should be passed as Path Variable
	 * @return ResponseWrapper<DocumentResponseDTO>
	 */
	@GetMapping(path = "/document/{document-id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseWrapper<DocumentDTO> getDocumentByDocumentId(
			@RequestParam("transactionId") String transactionId,
			@PathVariable("document-id") String documentId) {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Get document API"));
		ResponseWrapper<DocumentDTO> responseWrapper = new ResponseWrapper<>();
		try {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.GET_DOCUMENT_BY_DOC_ID, transactionId));
			validator.validateDocumentIdAndTransactionId(documentId, transactionId);
			DocumentDTO documentResponse = service.fetchDocumentByDocId(transactionId, documentId);
			responseWrapper.setResponse(documentResponse);
			responseWrapper.setId(residentGetDocumentId);
			responseWrapper.setVersion(residentGetDocumentVersion);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.GET_DOCUMENT_BY_DOC_ID_SUCCESS, transactionId));
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.GET_DOCUMENT_BY_DOC_ID_FAILED, transactionId));
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
		}
		return responseWrapper;
	}

	/**
	 * It deletes a document for a given transaction id and document id
	 *
	 * @param transactionId The transaction ID of the document
	 * @param documentId    The document ID of the document
	 * @return ResponseWrapper<ResponseDTO>
	 */
	@DeleteMapping(path = "/documents/{document-id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseWrapper<ResponseDTO> deleteDocument(
			@RequestParam("transactionId") String transactionId,
			@PathVariable("document-id") String documentId) {
		audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_REQUEST, "Delete document API"));
		ResponseWrapper<ResponseDTO> responseWrapper = new ResponseWrapper<>();
		try {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.DELETE_DOCUMENT, transactionId));
			validator.validateDocumentIdAndTransactionId(documentId, transactionId);
			responseWrapper.setId(residentDeleteId);
			responseWrapper.setVersion(residentDeleteVersion);
			ResponseDTO documentResponse = service
					.deleteDocument(transactionId, documentId);
			responseWrapper.setResponse(documentResponse);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.DELETE_DOCUMENT_SUCCESS, transactionId));
		} catch (ResidentServiceCheckedException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.DELETE_DOCUMENT_FAILED, transactionId));
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			responseWrapper.setId(residentDeleteId);
			responseWrapper.setVersion(residentDeleteVersion);
			responseWrapper.setErrors(List.of(new ServiceError(e.getErrorCode(), e.getErrorText())));
		}
		return responseWrapper;
	}

}
