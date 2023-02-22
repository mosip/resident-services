package io.mosip.resident.test.aspect;

import io.mosip.resident.aspect.LoginCheck;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentSessionRepository;
import io.mosip.resident.repository.ResidentUserRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Aspect test class for login redirect API
 * 
 * @author Ritik Jain
 */
@RunWith(MockitoJUnitRunner.class)
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

	@Mock
	private ResidentSessionRepository residentSessionRepository;

	@Mock
	private AuditUtil audit;

	@Before
	public void setup() throws ResidentServiceCheckedException, ApisResourceAccessException {
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		Collection<String> cookies = new ArrayList<>();
		cookies.add(
				"Authorization=eyJhbGciOiJSUzI1NiIsInR5cCIgO; Max-Age=1800000; Expires=Thu, 10-Nov-2022 05:05:02 GMT; Path=/; HttpOnly");
		cookies.add("id_token=eyJhbGciOiJSUzI1NiIsInR5cCIg; Path=/; Secure; HttpOnly");
		Mockito.when(response.getHeaders(Mockito.anyString())).thenReturn(cookies);
		Cookie[] requestCookies = new Cookie[1];
		Cookie cookie = new Cookie("Authorization", "eyJhbGciOiJSUzI1NiIsInR5cCIgO");
		requestCookies[0] = cookie;
		Mockito.when(request.getCookies()).thenReturn(requestCookies);
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("User-Agent:WINDOWS");
		Mockito.when(identityServiceImpl.getResidentIdaTokenFromAccessToken(Mockito.anyString())).thenReturn("282452929935769234295");
		ReflectionTestUtils.setField(loginCheck, "authTokenHeader", "Authorization");
		Mockito.when(identityServiceImpl.createSessionId()).thenReturn("123");
//		Mockito.when(residentUserRepository.findById(Mockito.anyString()))
//				.thenReturn(Optional.of(new ResidentUserEntity()));
	}

	@Test
	public void tearDown() throws ResidentServiceCheckedException, ApisResourceAccessException {
		Mockito.when(identityServiceImpl.createSessionId()).thenReturn(null);
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}

	@Test
	public void tearDownFailure() throws ResidentServiceCheckedException, ApisResourceAccessException {
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}
	@Test
	public void testGetUserDetailsWithMac() throws ResidentServiceCheckedException, ApisResourceAccessException {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("User-Agent:Mac");
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}

	@Test
	public void testGetUserDetailsWithX11() throws ResidentServiceCheckedException, ApisResourceAccessException {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("User-Agent:x11");
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}

	@Test
	public void testGetUserDetailsWithAndroid() throws ResidentServiceCheckedException, ApisResourceAccessException {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("User-Agent:Android");
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}

	@Test
	public void testGetUserDetailsWithIPhone() throws ResidentServiceCheckedException, ApisResourceAccessException {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("User-Agent:IPhone");
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}

	@Test
	public void testGetUserDetailsWithUnKnownDevice() throws ResidentServiceCheckedException, ApisResourceAccessException {
		Mockito.when(request.getHeader(Mockito.anyString())).thenReturn("User-Agent:Unknown");
		loginCheck.getUserDetails("aHR0cHM6Ly9yZXNpZGVudC5kZ", "ce0dfae2-5dc3-4c2b", "733d8aa0-a53b-42e1",
				"51a3f4c2-c029-490b.730-0c60476d94f2", "ce0dfae2-5dc3-4c2b", request, response);
	}

	@Test
	public void testOnLoginReqFailure(){
		loginCheck.onLoginReqFailure(null);
	}

	@Test
	public void testLogin(){
		loginCheck.login();
	}

	@Test
	public void testLogoutUser(){
		loginCheck.logoutUser();
	}

	@Test
	public void testValidateAdminToken(){
		loginCheck.validateAdminToken();
	}

	@Test
	public void testOnValidateTokenFailureFailed(){
		loginCheck.onValidateTokenFailure(new RuntimeException());
	}

	@Test
	public void testLoginRedirect(){
		loginCheck.loginRedirect();
	}

	@Test
	public void testOnLoginReqSuccess(){
		loginCheck.onLoginReq(null, null, null, response);
	}

	@Test
	public void testOnLoginReq(){
		response.setStatus(300);
		loginCheck.onLoginReq(null, null, null, response);
	}

	@Test
	public void testOnLoginFailure() {
		loginCheck.onLoginFailure(null);
	}

	@Test
	public void testOnLogoutSuccess() {
		loginCheck.onLogoutSuccess(null, null, response);
	}

	@Test
	public void testOnLogoutFailure() {
		loginCheck.onLogoutFailure(new RuntimeException());
	}

	@Test
	public void testOnValidateTokenSuccess() {
		loginCheck.onValidateTokenSuccess(request, response);
	}

	@Test
	public void testOnValidateTokenFailure() {
		Cookie[] cookies1 = new Cookie[1];
		Cookie cookie = new Cookie("id_token", "eyJhbGciOiJSUzI1NiIsInR5cCIgO");
		cookies1[0] = cookie;
		Mockito.when(request.getCookies()).thenReturn(cookies1);
		loginCheck.onValidateTokenSuccess(request, response);
	}

}
