package io.mosip.resident.aspect;

import static io.mosip.resident.constant.ResidentConstants.*;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
/**
 * Aspect class for Rest API calls metrics to record the response time metrics
 * 
 * @author Loganathan S
 */
@Component
@Aspect
@EnableAspectJAutoProxy
@ConditionalOnProperty(value = "resident.rest.client.metrics.aspect.enabled", havingValue = "true", matchIfMissing = false)
public class RestClientMetricsAspect {

	@Autowired
    private MeterRegistry registry;
	
	@Pointcut("execution(* io.mosip.resident.util.ResidentServiceRestClient.getApi(URI, Class<?>, MultiValueMap<String, String>)")
	public void getAPIMethodPointcut() {
	}

	@Around("getAPIMethodPointcut()")
	public Object getAPIAdvice(ProceedingJoinPoint jp) throws Throwable {
		return recordExecution(jp, HttpMethod.GET);
	}
	
	@Pointcut("execution(* io.mosip.resident.util.ResidentServiceRestClient.patchApi(String, MediaType, Object, Class<?>)")
	public void patchAPIMethodPointcut() {
	}

	@Around("patchAPIMethodPointcut()")
	public Object patchAPIAdvice(ProceedingJoinPoint jp) throws Throwable {
		return recordExecution(jp, HttpMethod.PATCH);
	}
	
	@Pointcut("execution(* io.mosip.resident.util.ResidentServiceRestClient.putApi(String, Object, Class<?>, MediaType)")
	public void putAPIMethodPointcut() {
	}

	@Around("putAPIMethodPointcut()")
	public Object putAPIAdvice(ProceedingJoinPoint jp) throws Throwable {
		return recordExecution(jp, HttpMethod.PUT);
	}
	
	@Pointcut("execution(* io.mosip.resident.util.ResidentServiceRestClient.postApi(String, MediaType, Object, Class<?>)")
	public void postAPIMethodPointcut() {
	}

	@Around("postAPIMethodPointcut()")
	public Object postAPIAdvice(ProceedingJoinPoint jp) throws Throwable {
		return recordExecution(jp, HttpMethod.POST);
	}


	private Object recordExecution(ProceedingJoinPoint jp, HttpMethod httpMethod) throws Throwable {
		String url = jp.getArgs()[0].toString();
		long start = System.nanoTime();
        Object result;
		try {
			result = jp.proceed();
	        recordTimer(httpMethod, url, start, "NONE");
		} catch (Throwable e) {
	        recordTimer(httpMethod, url, start, e.getClass().getSimpleName());
			throw e;
		}
        
		return result;
	}

	private void recordTimer(HttpMethod httpMethod, String url, long start, String error) {
		Timer timer = Timer.builder(REST_CLIENT_RESPONSE_TIME_ID)
        		.tag("label", REST_CLIENT_RESPONSE_TIME_DESCRIPTION)
        		.tag("httpMethod", httpMethod.name())
        		.tag("url", url)
        		.tag("status", error)
        		.register(registry);
		timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
	}
}
