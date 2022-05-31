package io.mosip.resident.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.DocumentResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.DocumentValidator;

/**
 * @author Manoj SP
 *
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class DocumentControllerTest {
	
	private static final String REQUEST_JSON = "{\"id\":\"string\",\"version\":\"string\",\"requesttime\":\"2022-05-08T09:52:18.151Z\",\"request\":{\"docCatCode\":\"poi\",\"docTypCode\":\"doc006\",\"langCode\":\"eng\"}}";

	@InjectMocks
	private DocumentController controller;

	@Mock
	private DocumentValidator validator;

	@Mock
	private DocumentService service;
	
	@Mock
	private AuditUtil audit;
	
	@Test
	public void testUploadDocumentsSuccess() throws ResidentServiceCheckedException {
		DocumentResponseDTO response = new DocumentResponseDTO();
		when(service.uploadDocument(any(), any(), any())).thenReturn(response );
		ResponseWrapper<DocumentResponseDTO> uploadDocuments = controller.uploadDocuments("", new MockMultipartFile("name", "abc".getBytes()), REQUEST_JSON);
		assertEquals(response, uploadDocuments.getResponse());
	}
	
	@Test
	public void testUploadDocumentsFailed() throws ResidentServiceCheckedException {
		when(service.uploadDocument(any(), any(), any())).thenThrow(new ResidentServiceCheckedException("", ""));
		ResponseWrapper<DocumentResponseDTO> uploadDocuments = controller.uploadDocuments("", new MockMultipartFile("name", "abc".getBytes()), REQUEST_JSON);
		assertEquals(uploadDocuments.getErrors().get(0).getErrorCode(), "");
		assertEquals(uploadDocuments.getErrors().get(0).getMessage(), "");
	}
	
	@Test
	public void testUploadDocumentsInvalidRequest() throws ResidentServiceCheckedException {
		ResponseWrapper<DocumentResponseDTO> uploadDocuments = controller.uploadDocuments("", new MockMultipartFile("name", "abc".getBytes()), "");
		assertEquals(uploadDocuments.getErrors().get(0).getErrorCode(), ResidentErrorCode.BAD_REQUEST.getErrorCode());
		assertEquals(uploadDocuments.getErrors().get(0).getMessage(), ResidentErrorCode.BAD_REQUEST.getErrorMessage());
	}
	
	@Test
	public void testGetDocumentsByTransactionIdSuccess() throws ResidentServiceCheckedException {
		DocumentResponseDTO response = new DocumentResponseDTO();
		when(service.fetchAllDocumentsMetadata(any())).thenReturn(List.of(response));
		ResponseWrapper<List<DocumentResponseDTO>> documentsByTransactionId = controller.getDocumentsByTransactionId("");
		assertEquals(List.of(response), documentsByTransactionId.getResponse());
	}
	
	@Test
	public void testGetDocumentsByTransactionIdFailed() throws ResidentServiceCheckedException {
		when(service.fetchAllDocumentsMetadata(any())).thenThrow(new ResidentServiceCheckedException("", ""));
		ResponseWrapper<List<DocumentResponseDTO>> documentsByTransactionId = controller.getDocumentsByTransactionId("");
		assertEquals(documentsByTransactionId.getErrors().get(0).getErrorCode(), "");
		assertEquals(documentsByTransactionId.getErrors().get(0).getMessage(), "");
	}
}
