package io.mosip.resident.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.commons.khazana.exception.ObjectStoreAdapterException;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * @author Manoj SP
 *
 */
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class ObjectStoreHelperTest {

	@InjectMocks
	private ObjectStoreHelper helper;
	
	@Mock
	private ResidentServiceRestClient restClient;
	
	@Mock
	private ApplicationContext context;
	
	@Mock
	private ObjectStoreAdapter adapter;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() {
		ReflectionTestUtils.setField(helper, "objectStoreAccountName", "objectStoreAccountName");
		ReflectionTestUtils.setField(helper, "objectStoreBucketName", "objectStoreBucketName");
		ReflectionTestUtils.setField(helper, "objectStoreAdapterName", "objectStoreAdapterName");
		ReflectionTestUtils.setField(helper, "applicationId", "applicationId");
		ReflectionTestUtils.setField(helper, "referenceId", "referenceId");
		ReflectionTestUtils.setField(helper, "encryptUri", "encryptUri");
		ReflectionTestUtils.setField(helper, "decryptUri", "decryptUri");
		when(context.getBean(any(), any(Class.class))).thenReturn(adapter);
		helper.setObjectStore(context);
		mockEncryptionDecryptionRestCall(Map.of("data", "abc"), null);
	}

	private void mockEncryptionDecryptionRestCall(Map<String, Object> data, List<ServiceError> errors) {
		ResponseWrapper<Map<String, Object>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(data);
		responseWrapper.setErrors(errors);
		try {
			when(restClient.postApi(any(), any(), any(), any())).thenReturn(responseWrapper);
		} catch (ApisResourceAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testPutObjectSuccess() throws IOException {
		ArgumentCaptor<String> objectStoreAccountName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectStoreBucketName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> source = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> process = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<InputStream> data = ArgumentCaptor.forClass(InputStream.class);
		helper.putObject("name", new ByteArrayInputStream("abc".getBytes()));
		verify(adapter).putObject(objectStoreAccountName.capture(), 
				objectStoreBucketName.capture(), 
				source.capture(),
				process.capture(),
				objectName.capture(), 
				data.capture()
				);
		assertEquals("objectStoreAccountName", objectStoreAccountName.getValue());
		assertEquals("abc", IOUtils.toString(data.getValue(), Charset.defaultCharset()));
		assertEquals("name", objectName.getValue());
		assertNull(source.getValue());
		assertNull(process.getValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPutObjectWithMetadataSuccess() throws IOException {
		ArgumentCaptor<String> objectStoreAccountName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectStoreBucketName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> source = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> process = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<InputStream> data = ArgumentCaptor.forClass(InputStream.class);
		ArgumentCaptor<Map<String, Object>> metadata = ArgumentCaptor.forClass(Map.class);
		helper.putObject("name", new ByteArrayInputStream("abc".getBytes()), Map.of("data", "data"));
		verify(adapter).putObject(objectStoreAccountName.capture(), 
				objectStoreBucketName.capture(), 
				source.capture(),
				process.capture(),
				objectName.capture(), 
				data.capture()
				);
		assertEquals("objectStoreAccountName", objectStoreAccountName.getValue());
		assertEquals("abc", IOUtils.toString(data.getValue(), Charset.defaultCharset()));
		assertEquals("name", objectName.getValue());
		assertNull(source.getValue());
		assertNull(process.getValue());
		verify(adapter).addObjectMetaData(objectStoreAccountName.capture(), 
				objectStoreBucketName.capture(), 
				source.capture(),
				process.capture(),
				objectName.capture(), 
				metadata.capture()
				);
		assertEquals(Map.of("data", "data"), metadata.getValue());
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testPutObjectException() throws IOException {
		when(adapter.putObject(any(), any(), any(), any(), any(), any())).thenThrow(new ObjectStoreAdapterException("", ""));
		helper.putObject("name", new ByteArrayInputStream("abc".getBytes()));
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testPutObjectEncryptionDecryptionRestCallFailed() throws IOException, ApisResourceAccessException {
		when(restClient.postApi(any(), any(), any(), any())).thenThrow(new ApisResourceAccessException());
		helper.putObject("name", new ByteArrayInputStream("abc".getBytes()));
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testPutObjectEncryptionDecryptionError() throws IOException {
		mockEncryptionDecryptionRestCall(null, List.of(new ServiceError("", "")));
		helper.putObject("name", new ByteArrayInputStream("abc".getBytes()));
	}
	
	@Test
	public void testGetObjectSuccess() throws IOException {
		ArgumentCaptor<String> objectStoreAccountName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectStoreBucketName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> source = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> process = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
		when(adapter.getObject(any(), any(), any(), any(), any()))
				.thenReturn(new ByteArrayInputStream("abc".getBytes()));
		helper.getObject("name");
		verify(adapter).getObject(objectStoreAccountName.capture(), 
				objectStoreBucketName.capture(), 
				source.capture(),
				process.capture(),
				objectName.capture()
				);
		assertEquals("objectStoreAccountName", objectStoreAccountName.getValue());
		assertEquals("name", objectName.getValue());
		assertNull(source.getValue());
		assertNull(process.getValue());
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testGetObjectException() throws IOException {
		when(adapter.getObject(any(), any(), any(), any(), any())).thenThrow(new ObjectStoreAdapterException("", ""));
		helper.getObject("name");
	}
	
	@Test
	public void testGetAllObjectSuccess() throws IOException {
		ArgumentCaptor<String> objectStoreAccountName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
		when(adapter.getAllObjects(any(), any()))
				.thenReturn(List.of());
		helper.getAllObjects("name");
		verify(adapter).getAllObjects(objectStoreAccountName.capture(), 
				objectName.capture()
				);
		assertEquals("objectStoreAccountName", objectStoreAccountName.getValue());
		assertEquals("name", objectName.getValue());
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testGetAllObjectException() throws IOException {
		when(adapter.getAllObjects(any(), any())).thenThrow(new ObjectStoreAdapterException("", ""));
		helper.getAllObjects("name");
	}
	
	@Test
	public void testGetMetadataSuccess() throws IOException {
		ArgumentCaptor<String> objectStoreAccountName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectStoreBucketName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> source = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> process = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
		when(adapter.getMetaData(any(), any(), any(), any(), any()))
				.thenReturn(Map.of());
		helper.getMetadata("name");
		verify(adapter).getMetaData(objectStoreAccountName.capture(), 
				objectStoreBucketName.capture(), 
				source.capture(),
				process.capture(),
				objectName.capture()
				);
		assertEquals("objectStoreAccountName", objectStoreAccountName.getValue());
		assertEquals("name", objectName.getValue());
		assertNull(source.getValue());
		assertNull(process.getValue());
	}
	
	@Test(expected = ResidentServiceException.class)
	public void testGetMetadataException() throws IOException {
		when(adapter.getMetaData(any(), any(), any(), any(), any())).thenThrow(new ObjectStoreAdapterException("", ""));
		helper.getMetadata("name");
	}

	@Test
	public void testDeleteObjectSuccess() throws IOException {
		ArgumentCaptor<String> objectStoreAccountName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectStoreBucketName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> source = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> process = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> objectName = ArgumentCaptor.forClass(String.class);
		helper.deleteObject("name");
		verify(adapter).deleteObject(objectStoreAccountName.capture(),
				objectStoreBucketName.capture(),
				source.capture(),
				process.capture(),
				objectName.capture()
		);
		assertEquals("objectStoreAccountName", objectStoreAccountName.getValue());
		assertEquals("name", objectName.getValue());
		assertNull(source.getValue());
		assertNull(process.getValue());
	}

	@Test(expected = ResidentServiceException.class)
	public void testDeleteObjectException() throws IOException {
		when(adapter.deleteObject(any(), any(), any(), any(), any())).thenThrow(new ObjectStoreAdapterException("", ""));
		helper.deleteObject("name");
	}
}
