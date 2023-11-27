package io.mosip.resident.interceptor;

import static io.mosip.resident.constant.ResidentConstants.REST_CLIENT_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.REST_CLIENT_RESPONSE_TIME_ID;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.mosip.resident.constant.ResidentConstants;

/**
 * @author Loganathan S
 */
@Component
@ConditionalOnProperty(value = ResidentConstants.RESIDENT_REST_TEMPLATE_METRICS_INTERCEPTOR_FILTER_ENABLED, havingValue = "true", matchIfMissing = false)
public class RestTemplateMetricsInterceptor implements ClientHttpRequestInterceptor {
	
	@Autowired
    private MeterRegistry registry;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex) throws IOException {
        Thread currentThread = Thread.currentThread();
		long start = System.nanoTime();
		
		try {
			 ClientHttpResponse response = ex.execute(req, reqBody);
			 if(req.getURI()!=null) {
				 recordTimer(Objects.requireNonNull(req.getMethod()), req.getURI().toString(), start, "NONE", response.getStatusCode(), response.getStatusText(), currentThread.getName());
			 }
			return response;
		} catch (Throwable e) {
			if(req.getURI()!=null) {
				recordTimer(Objects.requireNonNull(req.getMethod()), req.getURI().toString(), start, e.getClass().getSimpleName(), null, "Error", currentThread.getName());
			}
			throw e;
		}
    }
    
    private void recordTimer(HttpMethod httpMethod, String url, long start, String error, HttpStatus httpStatus, String statusText, String thread) {
		Timer timer = Timer.builder(REST_CLIENT_RESPONSE_TIME_ID)
        		.tag("label", REST_CLIENT_RESPONSE_TIME_DESCRIPTION)
        		.tag("httpMethod", httpMethod.name())
        		.tag("url", url)
        		.tag("httpStatus", httpStatus == null ? "NA" : httpStatus.toString())
        		.tag("statusText", statusText == null ? "NA" : statusText)
        		.tag("thread", thread)
        		.tag("error", error)
        		.tag("service", "resident")
        		.publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
        		.register(registry);
		timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
	}
}
