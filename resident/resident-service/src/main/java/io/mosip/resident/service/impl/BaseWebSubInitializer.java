package io.mosip.resident.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;

@Component
public class BaseWebSubInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerConfiguration.logConfig(BaseWebSubInitializer.class);

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * The task subsctiption delay.
     */
    @Value("${" + ResidentConstants.SUBSCRIPTIONS_DELAY_ON_STARTUP + ":60000}")
    private int taskSubsctiptionDelay;

    /**
     * The publisher.
     */
    @Autowired
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Autowired
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    @Value("${resident.websub.authtype-status.topic}")
    private String topic;

    @Value("${websub.publish.url}")
    private String publishUrl;

    @Value("${websub.hub.url}")
    private String hubUrl;

    @Value("${resident.websub.authtype-status.secret}")
    private String secret;

    @Value("${resident.websub.callback.authtype-status.url}")
    private String callbackUrl;

    @Value("${resident.websub.callback.authTransaction-status.url}")
    private String callbackAuthTransactionUrl;

    @Value("${resident.websub.authTransaction-status.topic}")
    private String authTransactionTopic;

    @Value("${resident.websub.authTransaction-status.secret}")
    private String authTransactionSecret;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("onApplicationEvent", "BaseWebSubInitializer", "Application is ready");
        taskScheduler.schedule(() -> {
            //Invoke topic registrations. This is done only once.
            //Note: With authenticated websub, only register topics which are only published by IDA
            tryRegisterTopicEvent(topic);
            tryRegisterTopicEvent(authTransactionTopic);
            //Init topic subscriptions
            initSubsriptions();
            authTransactionSubscription();
        }, new Date(System.currentTimeMillis() + taskSubsctiptionDelay));

    }

    public void authTransactionSubscription() {
        subscribe(authTransactionTopic, callbackAuthTransactionUrl, authTransactionSecret, hubUrl);
    }

    protected void tryRegisterTopicEvent(String eventTopic) {
        try {
            logger.debug(this.getClass().getCanonicalName(), "tryRegisterTopicEvent", "",
                    "Trying to register topic: " + eventTopic);
            publisher.registerTopic(eventTopic, publishUrl);
            logger.info(this.getClass().getCanonicalName(), "tryRegisterTopicEvent", "",
                    "Registered topic: " + eventTopic);
        } catch (Exception e) {
            logger.info(this.getClass().getCanonicalName(), "tryRegisterTopicEvent", e.getClass().toString(),
                    "Error registering tryRegisterTopicEvent: " + eventTopic + "\n" + e.getMessage());
        }
    }

    protected void initSubsriptions() {
        logger.debug("subscribe", "",
                "Trying to subscribe to topic: " + topic + " callback-url: "
                        + callbackUrl);
        subscribe(topic, callbackUrl, secret, hubUrl);
        logger.info("subscribe", "",
                "Subscribed to topic: " + topic);

    }

    private void subscribe(String topic, String callbackUrl, String secret, String hubUrl) {
        try {
            SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
            logger.debug("subscribe", "",
                    "Trying to subscribe to topic: " + topic + " callback-url: "
                            + callbackUrl);
            subscriptionRequest.setCallbackURL(callbackUrl);
            subscriptionRequest.setSecret(secret);
            subscriptionRequest.setTopic(topic);
            subscriptionRequest.setHubURL(hubUrl);
            subscribe.subscribe(subscriptionRequest);

            logger.info("subscribe", "",
                    "Subscribed to topic: " + topic);
        } catch (Exception e) {
            logger.info("subscribe", e.getClass().toString(),
                    "Error subscribing topic: " + topic + "\n" + e.getMessage());
            throw e;
        }
    }
}
