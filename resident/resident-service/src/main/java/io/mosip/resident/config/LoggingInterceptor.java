package io.mosip.resident.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.constant.ResidentConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kamesh Shekhar Prasad
 */
@Component
@ConditionalOnProperty(value = ResidentConstants.RESIDENT_REST_TEMPLATE_LOGGING_INTERCEPTOR_FILTER_ENABLED, havingValue = "true", matchIfMissing = false)
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private final Logger logger = LoggerConfiguration.logConfig(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
            HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex) throws IOException {

        Thread currentThread = Thread.currentThread();
        StackTraceElement[] stackTrace = currentThread.getStackTrace();
        String stackTraceString = Stream.of(stackTrace).map(String::valueOf).collect(Collectors.joining("\n"));

        long currentTime = System.currentTimeMillis();
        ClientHttpResponse response = ex.execute(req, reqBody);
        long currentTimeAfterExecution = System.currentTimeMillis();
        long timeDiff = currentTimeAfterExecution - currentTime;
        System.out.println(currentTime+ " "+currentTimeAfterExecution+" "+timeDiff+""+timeDiff+"ms");
        logger.debug("#rest-template-log#"+ ","+ req.getMethod() + ","+ req.getURI() + ","+timeDiff+"ms,"+stackTraceString);
        return response;
    }
}
