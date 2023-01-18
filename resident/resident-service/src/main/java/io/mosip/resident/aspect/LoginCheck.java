package io.mosip.resident.aspect;

import java.net.HttpCookie;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.entity.ResidentUserEntity;
import io.mosip.resident.repository.ResidentUserRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.util.Utility;

/**
 * Aspect class for login redirect API
 * 
 * @author Ritik Jain
 */
@Component
@Aspect
@EnableAspectJAutoProxy
public class LoginCheck {

	private static final String SET_COOKIE = "Set-Cookie";
	private static final String USER_AGENT = "User-Agent";
	private static final String WINDOWS = "Windows";
	private static final String MAC = "Mac";
	private static final String UNIX = "Unix";
	private static final String X11 = "x11";
	private static final String ANDROID = "Android";
	private static final String IPHONE = "IPhone";
	private static final CharSequence AUTHORIZATION_TOKEN = "Authorization";

	@Autowired
	private ResidentUserRepository residentUserRepository;
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;
	
	@Autowired
	private Utility utility;
	
	private static final Logger logger = LoggerConfiguration.logConfig(LoginCheck.class);

	@After("execution(* io.mosip.kernel.authcodeflowproxy.api.controller.LoginController.loginRedirect(..)) && args(redirectURI,state,sessionState,code,stateCookie,req,res)")
	public void getUserDetails(String redirectURI, String state, String sessionState, String code, String stateCookie,
			HttpServletRequest req, HttpServletResponse res) throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("LoginCheck::getUserDetails()::entry");
		String idaToken = "";
		Collection<String> cookies = res.getHeaders(SET_COOKIE);
		for (String cookie : cookies) {
			if (cookie.contains(AUTHORIZATION_TOKEN)) {
				Optional<String> authorizationCookie = getCookieValueFromHeader(cookie);
				if (authorizationCookie.isPresent()) {
					idaToken = identityServiceImpl.getResidentIdaTokenFromAccessToken(authorizationCookie.get());
				}
			}
		}

		if(idaToken!=null && !idaToken.isEmpty()) {
			Optional<ResidentUserEntity> userData = residentUserRepository.findById(idaToken);
			if (userData.isPresent()) {
				residentUserRepository.updateUserData(idaToken, DateUtils.getUTCCurrentDateTime(), utility.getClientIp(req),
						req.getRemoteHost(), getMachineType(req));
			} else {
				residentUserRepository.save(new ResidentUserEntity(idaToken, DateUtils.getUTCCurrentDateTime(),
						utility.getClientIp(req), req.getRemoteHost(), getMachineType(req)));
			}
		}
		logger.debug("LoginCheck::getUserDetails()::exit");
	}

	private Optional<String> getCookieValueFromHeader(String cookie) {
		logger.debug("LoginCheck::getCookieValueFromHeader()::entry");
		List<HttpCookie> httpCookieList = HttpCookie.parse(cookie);
		if (!httpCookieList.isEmpty()) {
			HttpCookie httpCookie = httpCookieList.get(0);
			String value = httpCookie.getValue();
			logger.debug("LoginCheck::getCookieValueFromHeader()::exit");
			return Optional.of(value);
		}
		logger.debug("LoginCheck::getCookieValueFromHeader()::exit - cookie is empty");
		return Optional.empty();
	}

	private String getMachineType(HttpServletRequest req) {
		logger.debug("LoginCheck::getMachineType()::entry");
		String userAgent = req.getHeader(USER_AGENT);

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
		logger.debug("LoginCheck::getMachineType()::exit");
		return os;
	}
}
