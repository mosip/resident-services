package io.mosip.resident.test.service;

import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.service.impl.BaseWebSubInitializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class BaseWebSubInitializerTest {

    @Mock
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @MockBean
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    @Mock
    ThreadPoolTaskScheduler taskScheduler;


    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }


    @Before
    public void setUp() throws Exception {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testApplicationEvent() {
        BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
            ReflectionTestUtils.setField(
                    baseWebSubInitializer, "taskScheduler", Mockito.mock(ThreadPoolTaskScheduler.class));
            ApplicationReadyEvent applicationReadyEvent = null;
        baseWebSubInitializer.onApplicationEvent(applicationReadyEvent);
    }

    private BaseWebSubInitializer createTestSubject() {
        BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer() {};

        ReflectionTestUtils.setField(baseWebSubInitializer, "taskScheduler", Mockito.mock(ThreadPoolTaskScheduler.class));
        return baseWebSubInitializer;
    }

    @Test
    public void testRegisterTopics() throws Exception {

        BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
        baseWebSubInitializer.authTransactionSubscription();
        ReflectionTestUtils.invokeMethod(baseWebSubInitializer, "authTransactionSubscription");
    }

    @Test
    public void testTryRegisterTopicEvents() throws Exception {
        BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
        baseWebSubInitializer.tryRegisterTopicEvent("Topic");
        ReflectionTestUtils.invokeMethod(baseWebSubInitializer, "tryRegisterTopicEvent", "Topic");
    }

    @Test
    public void testinitSubsriptions() throws Exception {
        BaseWebSubInitializer baseWebSubInitializer = new BaseWebSubInitializer();
        baseWebSubInitializer.initSubsriptions();
        ReflectionTestUtils.invokeMethod(baseWebSubInitializer, "initSubsriptions");
    }

}