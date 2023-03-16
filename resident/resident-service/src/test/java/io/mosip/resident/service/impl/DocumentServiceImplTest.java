package io.mosip.resident.service.impl;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.resident.dto.DocumentRequestDTO;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;

/**
 * 
 * @author M1063027 Rama Devi
 *
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class DocumentServiceImplTest {

	@InjectMocks
	private DocumentServiceImpl documentServiceImpl;

	@Mock
	private ObjectStoreHelper objectStoreHelper;

	@Mock
	private Environment environment;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void uploadDocumentTest() throws Exception {
		DocumentRequestDTO request = getDocumentRqtDto();
		MockMultipartFile file = new MockMultipartFile("test.pdf", "test.pdf", null, new byte[1100]);
		file.getOriginalFilename();
		request.setDocCatCode("POI");
		request.setLangCode("eng");
		request.setDocCatCode("poi");
		request.setReferenceId("abc123");
		assertNotNull(documentServiceImpl.uploadDocument("transactionId", file, request));
	}

	@Test
	public void fetchAllDocumentsMetadataTest() throws Exception {
		List<ObjectDto> allObjects = getAllObjects();
		Map<String, Object> metaData = getMetaData();
		Mockito.when(objectStoreHelper.getAllObjects("transactionId")).thenReturn(allObjects);
		Mockito.when(objectStoreHelper.getMetadata(Mockito.anyString())).thenReturn(metaData);
		assertNotNull(documentServiceImpl.fetchAllDocumentsMetadata("transactionId"));
	}

	@Test(expected = ResidentServiceException.class)
	public void testFetchAllDocumentsMetadataFailure() throws Exception{
		Map<String, Object> metaData = getMetaData();
		Mockito.when(objectStoreHelper.getAllObjects("transactionId")).thenReturn(null);
		assertNotNull(documentServiceImpl.fetchAllDocumentsMetadata("transactionId"));
	}

	@Test
	public void getDocumentsWithMetadataTest() throws Exception {
		List<ObjectDto> allObjects = getAllObjects();
		Map<String, Object> metaData = getMetaData();
		Mockito.when(objectStoreHelper.getAllObjects("transactionId")).thenReturn(allObjects);
		Mockito.when(objectStoreHelper.getMetadata(Mockito.anyString())).thenReturn(metaData);
		Mockito.when(objectStoreHelper.getObject(Mockito.anyString())).thenReturn("value");
		assertNotNull(documentServiceImpl.getDocumentsWithMetadata("transactionId"));
	}

	@Test
	public void testFetchDocumentByDocId() throws Exception {
		Mockito.when(objectStoreHelper.getObject(Mockito.anyString())).thenReturn("value");
		assertNotNull(documentServiceImpl.fetchDocumentByDocId("transactionId", "docId"));
	}

	@Test
	public void testDeleteDocumentSuccess() throws Exception {
		Mockito.when(objectStoreHelper.getObject(Mockito.anyString())).thenReturn("value");
		Mockito.when(objectStoreHelper.deleteObject(Mockito.anyString())).thenReturn(true);
		assertNotNull(documentServiceImpl.deleteDocument("transactionId", "documentId"));
	}

	@Test
	public void testDeleteDocumentFailure() throws Exception {
		Mockito.when(objectStoreHelper.getObject(Mockito.anyString())).thenReturn("value");
		Mockito.when(objectStoreHelper.deleteObject(Mockito.anyString())).thenReturn(false);
		assertNotNull(documentServiceImpl.deleteDocument("transactionId", "documentId"));
	}

	private DocumentRequestDTO getDocumentRqtDto() {
		DocumentRequestDTO request = new DocumentRequestDTO();
		request.setDocCatCode("DocCatCode");
		request.setLangCode("langCode");
		request.setDocCatCode("docCatCode");
		request.setDocTypCode("docTypCode");
		return request;
	}

	private List<ObjectDto> getAllObjects() {
		List<ObjectDto> allObjects = new ArrayList<ObjectDto>();
		ObjectDto objectDto = new ObjectDto();
		objectDto.setObjectName("objectName");
		allObjects.add(objectDto);
		return allObjects;
	}

	private Map<String, Object> getMetaData() {
		Map<String, Object> metaData = new HashMap<String, Object>();
		metaData.put("docid", "12345");
		metaData.put("docname", "text.txt");
		metaData.put("doccatcode", "1234567");
		metaData.put("doctypcode", "12345123");
		return metaData;

	}

}
