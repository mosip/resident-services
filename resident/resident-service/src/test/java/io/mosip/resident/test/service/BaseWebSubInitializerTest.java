package io.mosip.resident.test.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.service.impl.BaseWebSubInitializer;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration
public class BaseWebSubInitializerTest {

	@Mock
	private PublisherClient<String, Object, HttpHeaders> publisher;

	@Mock
	SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

	@Mock
	ThreadPoolTaskScheduler taskScheduler;

	@Mock
	BaseWebSubInitializer baseWebSubInitializer;

	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
		return threadPoolTaskScheduler;
	}

	@Value("${resident.websub.callback.authtype-status.url}")
	private String callbackUrl;

	@Value("${resident.websub.callback.authTransaction-status.url}")
	private String callbackAuthTransactionUrl;

	@Value("${resident.websub.authTransaction-status.topic}")
	private String authTransactionTopic;

	@Value("${resident.websub.authTransaction-status.secret}")
	private String authTransactionSecret;

	@Before
	public void setUp() throws Exception {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(5);
		taskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionSecret", "authTransactionSecret");
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionTopic",
				"AUTHENTICATION_TRANSACTION_STATUS");
		ReflectionTestUtils.setField(baseWebSubInitializer, "callbackAuthTransactionUrl",
				"resident.websub.callback.authTransaction-status.relative.url");
		ReflectionTestUtils.setField(baseWebSubInitializer, "hubUrl", "https://dev2.mosip.net/lib");
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testApplicationEvent() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
		ReflectionTestUtils.setField(baseWebSubInitializer, "taskScheduler",
				Mockito.mock(ThreadPoolTaskScheduler.class));
		ApplicationReadyEvent applicationReadyEvent = null;
		baseWebSubInitializer.onApplicationEvent(applicationReadyEvent);
	}

	@Test
	public void applicationEventTest() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
		ReflectionTestUtils.setField(baseWebSubInitializer, "taskScheduler",
				Mockito.mock(ThreadPoolTaskScheduler.class));
		ApplicationReadyEvent applicationReadyEvent = null;
		ReflectionTestUtils.setField(baseWebSubInitializer, "topic", "AUTH_TYPE_STATUS_UPDATE_ACK");
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionTopic",
				"AUTHENTICATION_TRANSACTION_STATUS");
		baseWebSubInitializer.onApplicationEvent(applicationReadyEvent);
	}

	@Test(expected = Exception.class)
	public void authTransactionSubscriptionTest() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionSecret", "authTransactionSecret");
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionTopic",
				"AUTHENTICATION_TRANSACTION_STATUS");
		ReflectionTestUtils.setField(baseWebSubInitializer, "callbackAuthTransactionUrl",
				"resident.websub.callback.authTransaction-status.relative.url");
		ReflectionTestUtils.setField(baseWebSubInitializer, "hubUrl", "https://dev2.mosip.net/lib");
		baseWebSubInitializer.authTransactionSubscription();
	}

	@Test
	public void testAuthTransactionSubcription() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionSecret", "authTransactionSecret");
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionTopic",
				"AUTHENTICATION_TRANSACTION_STATUS");
		ReflectionTestUtils.setField(baseWebSubInitializer, "callbackAuthTransactionUrl",
				"resident.websub.callback.authTransaction-status.relative.url");
		ReflectionTestUtils.setField(baseWebSubInitializer, "hubUrl", "https://dev2.mosip.net/lib");
		ReflectionTestUtils.setField(baseWebSubInitializer, "subscribe", subscribe);
		baseWebSubInitializer.authTransactionSubscription();
	}

	@Test
	public void testTryRegisterTopicEventFailed() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
		ReflectionTestUtils.setField(baseWebSubInitializer, "taskScheduler",
				Mockito.mock(ThreadPoolTaskScheduler.class));
		ReflectionTestUtils.invokeMethod(baseWebSubInitializer, "tryRegisterTopicEvent", "AUTH_TYPE_STATUS_UPDATE_ACK");
	}

	@Test
	public void testTryRegisterTopicEvent() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
		ReflectionTestUtils.setField(baseWebSubInitializer, "publishUrl", "https://dev2.mosip.net/lib");
		ReflectionTestUtils.setField(baseWebSubInitializer, "publisher", publisher);
		ReflectionTestUtils.invokeMethod(baseWebSubInitializer, "tryRegisterTopicEvent", "AUTH_TYPE_STATUS_UPDATE_ACK");
	}

	@Test
	public void testInitSubsription() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionSecret", "authTransactionSecret");
		ReflectionTestUtils.setField(baseWebSubInitializer, "authTransactionTopic",
				"AUTHENTICATION_TRANSACTION_STATUS");
		ReflectionTestUtils.setField(baseWebSubInitializer, "callbackAuthTransactionUrl",
				"resident.websub.callback.authTransaction-status.relative.url");
		ReflectionTestUtils.setField(baseWebSubInitializer, "subscribe", subscribe);
		ReflectionTestUtils.setField(baseWebSubInitializer, "hubUrl", "https://dev2.mosip.net/lib");
		ReflectionTestUtils.invokeMethod(baseWebSubInitializer, "initSubsriptions");
	}

	private BaseWebSubInitializer testTaskScheduler() {
		BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer() {
		};
		ReflectionTestUtils.setField(baseWebSubInitializer, "taskScheduler",
				Mockito.mock(ThreadPoolTaskScheduler.class));
		return baseWebSubInitializer;
	}

}