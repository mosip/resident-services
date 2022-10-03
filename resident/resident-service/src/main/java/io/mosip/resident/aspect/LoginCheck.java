package io.mosip.resident.aspect;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Aspect
@EnableAspectJAutoProxy
public class LoginCheck {
	
	private final String LOCALHOST_IPV4 = "127.0.0.1";
	private final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
	
	@After("execution(* io.mosip.kernel.authcodeflowproxy.api.controller.LoginController.loginRedirect(..)) && args(redirectURI,state,sessionState,code,stateCookie,req,res)")
	public void getUserDetails(String redirectURI, String state, String sessionState, String code, String stateCookie, HttpServletRequest req, HttpServletResponse res) {
		System.out.println("Running After Advice.============================"+redirectURI+"=============="+state+"----"+sessionState+"======--"+code);
		System.out.println("==============="+res.getHeaders("Set-Cookie")+"====================");
		System.out.println("ip address: "+getClientIp(req));
		System.out.println("OS type: "+getMachineType(req));
		
//		get http or https and host
		req.getScheme();
		req.getServerName();
		
//		method to get host-------
		System.out.println("host: "+req.getHeader("host"));
		
//		other method---------
		System.out.println("==================================="+req.getRequestURL().toString().replace(req.getRequestURI(), ""));
		
//		another method-------
		StringBuffer url = req.getRequestURL();
		String uri = req.getRequestURI();
		int idx = (((uri != null) && (uri.length() > 0)) ? url.indexOf(uri) : url.length());
		String host = url.substring(0, idx); //base url
		idx = host.indexOf("://");
		if(idx > 0) {
		  host = host.substring(idx); //remove scheme if present
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

	public String getClientIp(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-Forwarded-For");
		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		
		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		
		if(StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if(LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
				try {
					InetAddress inetAddress = InetAddress.getLocalHost();
					ipAddress = inetAddress.getHostAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(!StringUtils.isEmpty(ipAddress) 
				&& ipAddress.length() > 15
				&& ipAddress.indexOf(",") > 0) {
			ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
		}
		
		return ipAddress;
	}

}
