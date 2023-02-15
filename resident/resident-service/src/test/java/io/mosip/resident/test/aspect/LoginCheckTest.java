package io.mosip.resident.test.aspect;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;

import io.mosip.resident.aspect.LoginCheck;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentUserRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.Utility;

/**
 * Aspect test class for login redirect API
 * 
 * @author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
@RefreshScope
@ContextConfiguration
public class LoginCheckTest {

	@InjectMocks
	private LoginCheck loginCheck;

	@Mock
	private ResidentUserRepository residentUserRepository;
	
	@Mock
	private IdentityServiceImpl identityServiceImpl;
	
	@Mock
	private Utility utility;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Before
	public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException {
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		Collection<String> cookies = new ArrayList<>();
		cookies.add(
				"Authorization=eyJhbGciOiJSUzI1NiIsInR5cCIgO; Max-Age=1800000; Expires=Thu, 10-Nov-2022 05:05:02 GMT; Path=/; HttpOnly");
		cookies.add("id_token=eyJhbGciOiJSUzI1NiIsInR5cCIg; Path=/; Secure; HttpOnly");
		Mockito.when(response.getHeaders(Mockito.anyString())).thenReturn(cookies);
		Mockito.when(identityServiceImpl.getResidentIdaTokenFromAccessToken(Mockito.anyString())).thenReturn("282452929935769234295");
//		Mockito.when(residentUserRepository.findById(Mockito.anyString()))
//				.thenReturn(Optional.of(new ResidentUserEntity()));
	}

	@After
	public void tearDown() throws ResidentServiceCheckedException, ApisResourceAccessException {
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}

//	@Test
//	public void testGetUserDetails() {
//		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("Windows");
//	}

//	@Test
//	public void testGetUserDetailsNewEntryInDB() {
//		Mockito.when(residentUserRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
//		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("");
//		Mockito.when(request.getRemoteHost()).thenReturn("1.2.3");
//	}

//	@Test
//	public void testGetMachineTypeMac() {
//		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("Mac");
//	}
//
//	@Test
//	public void testGetMachineTypeUnix() {
//		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("x11");
//	}
//
//	@Test
//	public void testGetMachineTypeAndroid() {
//		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("Android");
//	}
//
//	@Test
//	public void testGetMachineTypeIphone() {
//		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("IPhone");
//	}

}
