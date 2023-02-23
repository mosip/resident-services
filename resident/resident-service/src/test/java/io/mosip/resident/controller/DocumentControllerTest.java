package io.mosip.resident.controller;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.DocumentDTO;
import io.mosip.resident.dto.DocumentResponseDTO;
import io.mosip.resident.dto.ResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.DocumentService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.validator.DocumentValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Manoj SP
 *
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class DocumentControllerTest {
	
	private static final String REQUEST_JSON = "eyJpZCI6InN0cmluZyIsInZlcnNpb24iOiJzdHJpbmciLCJyZXF1ZXN0dGltZSI6IjIwMjItMDUtMDhUMDk6NTI6MTguMTUxWiIsInJlcXVlc3QiOnsiZG9jQ2F0Q29kZSI6InBvaSIsImRvY1R5cENvZGUiOiJkb2MwMDYiLCJsYW5nQ29kZSI6ImVuZyJ9fQ";

	@InjectMocks
	private DocumentController controller;

	@Mock
	private DocumentValidator validator;

	@Mock
	private DocumentService service;
	
	@Mock
	private AuditUtil audit;
	
	@Test
	public void testUploadDocumentsSuccess() throws ResidentServiceCheckedException, IOException {
		DocumentResponseDTO response = new DocumentResponseDTO();
		when(service.uploadDocument(any(), any(), any())).thenReturn(response );
		ResponseWrapper<DocumentResponseDTO> uploadDocuments = controller.uploadDocuments("", new MockMultipartFile("name", "abc".getBytes()), "poi", "proof", "eng", "abc123");
		assertEquals(response, uploadDocuments.getResponse());
	}
	
	@Test
	public void testUploadDocumentsFailed() throws ResidentServiceCheckedException, IOException {
		when(service.uploadDocument(any(), any(), any())).thenThrow(new ResidentServiceCheckedException("", ""));
		ResponseWrapper<DocumentResponseDTO> uploadDocuments = controller.uploadDocuments("", new MockMultipartFile("name", "abc".getBytes()), "poi", "proof", "eng", "abc123");
		assertEquals(uploadDocuments.getErrors().get(0).getErrorCode(), "");
		assertEquals(uploadDocuments.getErrors().get(0).getMessage(), "");
	}
		
	@Test
	public void testGetDocumentsByTransactionIdSuccess() throws ResidentServiceCheckedException {
		DocumentResponseDTO response = new DocumentResponseDTO();
		ReflectionTestUtils.setField(controller, "residentDocumentListId", "mosip.resident.document.list");
		ReflectionTestUtils.setField(controller, "residentDocumentListVersion", "v1");
		when(service.fetchAllDocumentsMetadata(any())).thenReturn(List.of(response));
		ResponseWrapper<List<DocumentResponseDTO>> documentsByTransactionId = controller.getDocumentsByTransactionId("");
		assertEquals(List.of(response), documentsByTransactionId.getResponse());
		assertEquals("mosip.resident.document.list", documentsByTransactionId.getId());
		assertEquals("v1", documentsByTransactionId.getVersion());
	}
	
	@Test
	public void testGetDocumentsByTransactionIdFailed() throws ResidentServiceCheckedException {
		when(service.fetchAllDocumentsMetadata(any())).thenThrow(new ResidentServiceCheckedException("", ""));
		ResponseWrapper<List<DocumentResponseDTO>> documentsByTransactionId = controller.getDocumentsByTransactionId("");
		assertEquals(documentsByTransactionId.getErrors().get(0).getErrorCode(), "");
		assertEquals(documentsByTransactionId.getErrors().get(0).getMessage(), "");
	}

	@Test
	public void testGetDocumentByDocumentIdSuccess() throws ResidentServiceCheckedException {
		DocumentDTO response = new DocumentDTO();
		ReflectionTestUtils.setField(controller, "residentGetDocumentId", "mosip.resident.document.get");
		ReflectionTestUtils.setField(controller, "residentGetDocumentVersion", "v1");
		validator.validateTransactionIdForDocument("123");
		when(service.fetchDocumentByDocId(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
		ResponseWrapper<DocumentDTO> documentByDocumentId = controller.getDocumentByDocumentId("", "");
		assertEquals(response, documentByDocumentId.getResponse());
		assertEquals("mosip.resident.document.get", documentByDocumentId.getId());
		assertEquals("v1", documentByDocumentId.getVersion());
	}

	@Test
	public void testGetDocumentByDocumentIdFailed() throws ResidentServiceCheckedException {
		when(service.fetchDocumentByDocId(any(), any())).thenThrow(new ResidentServiceCheckedException("", ""));
		ResponseWrapper<DocumentDTO> documentByDocumentId = controller.getDocumentByDocumentId("", "");
		assertEquals(documentByDocumentId.getErrors().get(0).getErrorCode(), "");
		assertEquals(documentByDocumentId.getErrors().get(0).getMessage(), "");
	}

	@Test
	public void testDeleteDocumentsByDocumentIdSuccess() throws ResidentServiceCheckedException {
		ResponseDTO response = new ResponseDTO();
		when(service.deleteDocument(Mockito.anyString(), Mockito.anyString())).thenReturn(response);
		ResponseWrapper<ResponseDTO> deleteDocumentsByDocumentId = controller.deleteDocument("", "");
		assertEquals(response, deleteDocumentsByDocumentId.getResponse());
	}

	@Test
	public void testDeleteDocumentsByDocumentIdFailed() throws ResidentServiceCheckedException {
		when(service.deleteDocument(Mockito.anyString(), Mockito.anyString())).thenThrow(new ResidentServiceCheckedException("", ""));
		ResponseWrapper<ResponseDTO> deleteDocumentsByDocumentId = controller.deleteDocument("", "");
		assertEquals(deleteDocumentsByDocumentId.getErrors().get(0).getErrorCode(), "");
		assertEquals(deleteDocumentsByDocumentId.getErrors().get(0).getMessage(), "");
	}
}
