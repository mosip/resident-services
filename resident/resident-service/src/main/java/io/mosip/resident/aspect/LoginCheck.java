package io.mosip.resident.aspect;

import java.net.HttpCookie;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.entity.ResidentUserEntity;
import io.mosip.resident.repository.ResidentUserRepository;

/**
 * Aspect class for login redirect API
 * 
 * @author Ritik Jain
 */
@Component
@Aspect
@EnableAspectJAutoProxy
public class LoginCheck {

	private static final String ID_TOKEN = "id_token";
	private static final String SET_COOKIE = "Set-Cookie";
	private static final String USER_AGENT = "User-Agent";
	private static final String WINDOWS = "Windows";
	private static final String MAC = "Mac";
	private static final String UNIX = "Unix";
	private static final String X11 = "x11";
	private static final String ANDROID = "Android";
	private static final String IPHONE = "IPhone";
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
	private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
	private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
	private static final String HTTP_X_FORWARDED = "HTTP_X_FORWARDED";
	private static final String HTTP_X_CLUSTER_CLIENT_IP = "HTTP_X_CLUSTER_CLIENT_IP";
	private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
	private static final String HTTP_FORWARDED_FOR = "HTTP_FORWARDED_FOR";
	private static final String HTTP_FORWARDED = "HTTP_FORWARDED";
	private static final String HTTP_VIA = "HTTP_VIA";
	private static final String REMOTE_ADDR = "REMOTE_ADDR";

	@Autowired
	private ResidentUserRepository residentUserRepository;

	@After("execution(* io.mosip.kernel.authcodeflowproxy.api.controller.LoginController.loginRedirect(..)) && args(redirectURI,state,sessionState,code,stateCookie,req,res)")
	public void getUserDetails(String redirectURI, String state, String sessionState, String code, String stateCookie,
			HttpServletRequest req, HttpServletResponse res) {
		String idaToken = "";
		Collection<String> cookies = res.getHeaders(SET_COOKIE);
		for (String cookie : cookies) {
			if (cookie.contains(ID_TOKEN)) {
				Optional<String> cookieIdToken = getCookieValueFromHeader(cookie);
				if (cookieIdToken.isPresent()) {
					idaToken = getIdaTokenFromIdToken(cookieIdToken.get()); // call a method to get ida token from id_token
				}
			}
		}

		Optional<ResidentUserEntity> userData = residentUserRepository.findById(idaToken);
		if (userData.isPresent()) {
			residentUserRepository.updateUserData(idaToken, DateUtils.getUTCCurrentDateTime(), getClientIp(req),
					req.getRemoteHost(), getMachineType(req));
		} else {
			residentUserRepository.save(new ResidentUserEntity(idaToken, DateUtils.getUTCCurrentDateTime(),
					getClientIp(req), req.getRemoteHost(), getMachineType(req)));
		}
	}

	private String getIdaTokenFromIdToken(String cookieIdToken) {
		// TODO Auto-generated method stub
		return "283806899483626793628536705744577345";
	}

	private Optional<String> getCookieValueFromHeader(String cookie) {
		List<HttpCookie> httpCookieList = HttpCookie.parse(cookie);
		if (!httpCookieList.isEmpty()) {
			HttpCookie httpCookie = httpCookieList.get(0);
			String value = httpCookie.getValue();
			return Optional.of(value);
		}
		return Optional.empty();
	}

	private String getMachineType(HttpServletRequest req) {
		String browserDetails = req.getHeader(USER_AGENT);
		String userAgent = browserDetails;

		String os = "";
		if (userAgent.toLowerCase().indexOf(WINDOWS.toLowerCase()) >= 0) {
			os = WINDOWS;
		} else if (userAgent.toLowerCase().indexOf(MAC.toLowerCase()) >= 0) {
			os = MAC;
		} else if (userAgent.toLowerCase().indexOf(X11) >= 0) {
			os = UNIX;
		} else if (userAgent.toLowerCase().indexOf(ANDROID.toLowerCase()) >= 0) {
			os = ANDROID;
		} else if (userAgent.toLowerCase().indexOf(IPHONE.toLowerCase()) >= 0) {
			os = IPHONE;
		} else {
			os = "UnKnown, More-Info: " + userAgent;
		}
		return os;
	}

	public String getClientIp(HttpServletRequest req) {
		String[] IP_HEADERS = {
				X_FORWARDED_FOR,
				PROXY_CLIENT_IP,
				WL_PROXY_CLIENT_IP,
				HTTP_X_FORWARDED_FOR,
				HTTP_X_FORWARDED,
				HTTP_X_CLUSTER_CLIENT_IP,
				HTTP_CLIENT_IP,
				HTTP_FORWARDED_FOR,
				HTTP_FORWARDED,
				HTTP_VIA,
				REMOTE_ADDR
		};
		for (String header : IP_HEADERS) {
			String value = req.getHeader(header);
			if (value == null || value.isEmpty()) {
				continue;
			}
			String[] parts = value.split("\\s*,\\s*");
			return parts[0];
		}
		return req.getRemoteAddr();
	}

}
