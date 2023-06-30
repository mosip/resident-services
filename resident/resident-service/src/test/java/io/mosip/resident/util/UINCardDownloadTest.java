package io.mosip.resident.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import io.mosip.resident.constant.IdType;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.UINCardDownloadService;

@RunWith(MockitoJUnitRunner.class)
public class UINCardDownloadTest {
	@Mock
    private Environment env;

	@Mock
    private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
    UINCardDownloadService uinCardDownloadService;
	byte[] arr= new byte[10];
	String res="{\"errors\":[{\"message\":\"error occured\"}]}";
	@Test
	public void testgetUINCard() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(arr);
		assertEquals(arr,uinCardDownloadService.getUINCard("123456789", "UIN", IdType.UIN));
	}
	@Test(expected=ApisResourceAccessException.class)
	public void testgetUINCardregprocfailure() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(res.getBytes());
		assertEquals(arr,uinCardDownloadService.getUINCard("123456789", "UIN", IdType.UIN));
	}
	@Test(expected=ApisResourceAccessException.class)
	public void testgetUINCardregprocNull() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
		assertEquals(arr,uinCardDownloadService.getUINCard("123456789", "UIN", IdType.UIN));
	}
	@Test(expected=ApisResourceAccessException.class)
	public void testgetUINCardFailure() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new ApisResourceAccessException());
		uinCardDownloadService.getUINCard("123456789", "UIN", IdType.UIN);
	}

}
