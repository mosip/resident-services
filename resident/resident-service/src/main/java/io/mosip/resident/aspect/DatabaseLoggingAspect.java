package io.mosip.resident.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;

/**
 * Aspect class for database logging
 * 
 * @author Ritik Jain
 */
@Component
@Aspect
@EnableAspectJAutoProxy
@ConditionalOnProperty(value = "resident.db.logging.aspect.enabled", havingValue = "true", matchIfMissing = false)
public class DatabaseLoggingAspect {

	private static final Logger logger = LoggerConfiguration.logConfig(DatabaseLoggingAspect.class);

	@Pointcut("execution(* io.mosip.resident.repository.ResidentTransactionRepository.*(..))")
	public void loggableMethods() {
	}

	@Around("loggableMethods()")
	public Object aroundAdvice(ProceedingJoinPoint jp) throws Throwable {
		logger.debug("Executing method => " + jp.getSignature());
		long beforeExecutionTime = System.currentTimeMillis();
		Object result = jp.proceed();
		long afterExecutionTime = System.currentTimeMillis();
		logger.debug(
				"Time taken by '" + jp.getSignature() + "' is " + (afterExecutionTime - beforeExecutionTime) + "ms.");
		return result;
	}
}
