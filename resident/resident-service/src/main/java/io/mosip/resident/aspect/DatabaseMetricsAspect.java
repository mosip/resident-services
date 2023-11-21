package io.mosip.resident.aspect;

import static io.mosip.resident.constant.ResidentConstants.DB_QUERY_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.DB_QUERY_RESPONSE_TIME_ID;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
/**
 * Aspect class for database metrics to record the query time metrics
 * 
 * @author Loganathan S
 */
@Component
@Aspect
@EnableAspectJAutoProxy
@ConditionalOnProperty(value = "resident.db.metrics.aspect.enabled", havingValue = "true", matchIfMissing = false)
public class DatabaseMetricsAspect {

	@Autowired
    private MeterRegistry registry;
	
	@Pointcut("execution(* io.mosip.resident.repository.ResidentTransactionRepository.*(..))")
	public void repositoryMethods() {
	}

	@Around("repositoryMethods()")
	public Object aroundAdvice(ProceedingJoinPoint jp) throws Throwable {
        Timer timer = Timer.builder(DB_QUERY_RESPONSE_TIME_ID)
        		.tag("label", DB_QUERY_RESPONSE_TIME_DESCRIPTION)
        		.tag("queryMethod", jp.getSignature().toShortString())
        		.tag("service", "resident")
        		.publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
        		.register(registry);
        long start = System.nanoTime();
        Object result = jp.proceed();
		timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
		return result;
	}
}
