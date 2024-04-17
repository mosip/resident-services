package io.mosip.resident.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.exception.ApisResourceAccessException;

@RunWith(MockitoJUnitRunner.class)
public class UINCardDownloadTest {
	@Mock
    private Environment env;

	@Mock
    private ResidentServiceRestClient residentServiceRestClient;

	@InjectMocks
    UINCardDownloadHelper uinCardDownloadHelper;
	byte[] arr= new byte[10];
	String res="{\"errors\":[{\"message\":\"error occured\"}]}";
	@Test
	public void testgetUINCard() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(arr);
		assertEquals(arr,uinCardDownloadHelper.getUINCard("123456789", CardType.UIN.name(), IdType.UIN));
	}
	@Test(expected=ApisResourceAccessException.class)
	public void testgetUINCardregprocfailure() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(res.getBytes());
		assertEquals(arr,uinCardDownloadHelper.getUINCard("123456789", CardType.UIN.name(), IdType.UIN));
	}
	@Test(expected=ApisResourceAccessException.class)
	public void testgetUINCardregprocNull() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
		assertEquals(arr,uinCardDownloadHelper.getUINCard("123456789", CardType.UIN.name(), IdType.UIN));
	}
	@Test(expected=ApisResourceAccessException.class)
	public void testgetUINCardFailure() throws ApisResourceAccessException {
		Mockito.when(residentServiceRestClient.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new ApisResourceAccessException());
		uinCardDownloadHelper.getUINCard("123456789", CardType.UIN.name(), IdType.UIN);
	}

}
