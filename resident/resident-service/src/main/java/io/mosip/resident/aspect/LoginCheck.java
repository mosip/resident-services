package io.mosip.resident.aspect;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.entity.ResidentUserEntity;
import io.mosip.resident.repository.ResidentUserRepository;

@Component
@Aspect
@EnableAspectJAutoProxy
public class LoginCheck {
	
	@Autowired
	private ResidentUserRepository residentUserRepository;
	
//	private final String LOCALHOST_IPV4 = "127.0.0.1";
//	private final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
	
	@After("execution(* io.mosip.kernel.authcodeflowproxy.api.controller.LoginController.loginRedirect(..)) && args(redirectURI,state,sessionState,code,stateCookie,req,res)")
	public void getUserDetails(String redirectURI, String state, String sessionState, String code, String stateCookie, HttpServletRequest req, HttpServletResponse res) {
		System.out.println("Running After Advice.============================"+redirectURI+"=============="+state+"----"+sessionState+"======--"+code);
		
		Collection<String> cookies=res.getHeaders("Set-Cookie");
		cookies.forEach(cookie -> {
			if(cookie.contains("id_token"))
				System.out.println("================================"+cookie);
		});
		
		System.out.println("ip address: "+getClientIp(req));
		System.out.println("OS type: "+getMachineType(req));
		System.out.println("Hostname: "+req.getRemoteHost());
		System.out.println("Date/Time: "+DateUtils.getUTCCurrentDateTimeString());
		
//		Optional<ResidentUserEntity> userData = residentUserRepository.findById(idaToken);
		Optional<ResidentUserEntity> userData = residentUserRepository.findById("283806899483626793628536705744577345");
		if(userData.isPresent()) {
//			residentUserRepository.updateUserData("idaToken", "lastLoginTime", "clientIP", "host", "machineType");
			residentUserRepository.updateUserData("283806899483626793628536705744577345",
					DateUtils.getUTCCurrentDateTime(), getClientIp(req), req.getRemoteHost(),
					getMachineType(req));
		} else {
//			residentUserRepository.save(new ResidentUserEntity("idaToken", null, "lastLoginTime", "clientIP", "host", "machineType"));
			residentUserRepository.save(new ResidentUserEntity("283806899483626793628536705744577345", null,
					DateUtils.getUTCCurrentDateTime(), getClientIp(req), req.getRemoteHost(),
					getMachineType(req)));
		}
	}
	
	private String getMachineType(HttpServletRequest req) {
		String  browserDetails  =   req.getHeader("User-Agent");
        String  userAgent       =   browserDetails;
    
        String os = "";
        //=================OS=======================
         if (userAgent.toLowerCase().indexOf("windows") >= 0 )
         {
             os = "Windows";
         } else if(userAgent.toLowerCase().indexOf("mac") >= 0)
         {
             os = "Mac";
         } else if(userAgent.toLowerCase().indexOf("x11") >= 0)
         {
             os = "Unix";
         } else if(userAgent.toLowerCase().indexOf("android") >= 0)
         {
             os = "Android";
         } else if(userAgent.toLowerCase().indexOf("iphone") >= 0)
         {
             os = "IPhone";
         }else{
             os = "UnKnown, More-Info: "+userAgent;
         }
		return os;
	}

//	public String getClientIp(HttpServletRequest request) {
//		String ipAddress = request.getHeader("X-Forwarded-For");
//		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
//			ipAddress = request.getHeader("Proxy-Client-IP");
//		}
//		
//		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
//			ipAddress = request.getHeader("WL-Proxy-Client-IP");
//		}
//		
//		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
//			ipAddress = request.getRemoteAddr();
//			if(LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
//				try {
//					InetAddress inetAddress = InetAddress.getLocalHost();
//					ipAddress = inetAddress.getHostAddress();
//				} catch (UnknownHostException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		if(!StringUtils.isEmpty(ipAddress) 
//				&& ipAddress.length() > 15
//				&& ipAddress.indexOf(",") > 0) {
//			ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
//		}
//		
//		return ipAddress;
//	}
	
	public String getClientIp(HttpServletRequest req) {
		String[] IP_HEADERS = {
		        "X-Forwarded-For",
		        "Proxy-Client-IP",
		        "WL-Proxy-Client-IP",
		        "HTTP_X_FORWARDED_FOR",
		        "HTTP_X_FORWARDED",
		        "HTTP_X_CLUSTER_CLIENT_IP",
		        "HTTP_CLIENT_IP",
		        "HTTP_FORWARDED_FOR",
		        "HTTP_FORWARDED",
		        "HTTP_VIA",
		        "REMOTE_ADDR"

		        // you can add more matching headers here ...
		    };
		for (String header: IP_HEADERS) {
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
