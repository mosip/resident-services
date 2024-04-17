package io.mosip.resident.util;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Since @Async on the AuditUtil class is causing bean loading issue, this async
 * util is created to delegate the Async bean creation to this class.
 * 
 * @author Loganathan S
 *
 */
@Component
public class AsyncUtil {
	
	@Async("AsyncExecutor")
	public void asyncRun(Runnable runnable) {
		runnable.run();
	}

}
