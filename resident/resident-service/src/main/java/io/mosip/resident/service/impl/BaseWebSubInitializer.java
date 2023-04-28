package io.mosip.resident.service.impl;

import java.time.Duration;
import java.time.Instant;
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
    private int taskSubscriptionInitialDelay;
    
    @Value("${" + ResidentConstants.RESUBSCRIPTIONS_INTERVAL_SECS + ":43200}")
    private int reSubscriptionIntervalSecs;

    /**
     * The publisher.
     */
    @Autowired
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Autowired
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    @Value("${websub.publish.url}")
    private String publishUrl;

    @Value("${websub.hub.url}")
    private String hubUrl;

    @Value("${resident.websub.authtype-status.secret}")
    private String authTypeStatusSecret;

    @Value("${resident.websub.callback.authtype-status.url}")
    private String authTypeStatusCallbackUrl;

    @Value("${resident.websub.callback.authTransaction-status.url}")
    private String authTransactionCallbackUrl;
    
    @Value("${resident.websub.authtype-status.topic}")
    private String autTypeStatusTopic;

    @Value("${resident.websub.authTransaction-status.topic}")
    private String authTransactionTopic;

    @Value("${resident.websub.authTransaction-status.secret}")
    private String authTransactionSecret;
    

    @Value("${resident.websub.callback.credential-status.url}")
    private String credentialStatusUpdateCallbackUrl;
    
    @Value("${resident.websub.credential-status.topic}")
    private String credentialStatusUpdateTopic;

    @Value("${resident.websub.credential-status.secret}")
    private String credentialStatusUpdateSecret;
    
    

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("onApplicationEvent", "BaseWebSubInitializer", "Application is ready");
		logger.info("Scheduling event subscriptions after (milliseconds): " + taskSubscriptionInitialDelay);
        taskScheduler.schedule(() -> {
            //Invoke topic registrations. This is done only once.
            //Note: With authenticated websub, only register topics which are only published by IDA
            tryRegisteringTopics();
            //Init topic subscriptions
            initTopicSubscriptions();
        }, new Date(System.currentTimeMillis() + taskSubscriptionInitialDelay));
        
        if (reSubscriptionIntervalSecs > 0) {
			logger.info("Work around for web-sub notification issue after some time.");
			scheduleRetrySubscriptions();
		} else {
			logger.info("Scheduling for re-subscription is Disabled as the re-subsctription delay value is: "
							+ reSubscriptionIntervalSecs);
		}

    }

	private void initTopicSubscriptions() {
		authTypStatusTopicSubsriptions();
		authTransactionTopicSubscription();
		credentialStatusUpdateTopicSubscription();
	}

	private void tryRegisteringTopics() {
		tryRegisterTopicEvent(autTypeStatusTopic);
		tryRegisterTopicEvent(authTransactionTopic);
		tryRegisterTopicEvent(credentialStatusUpdateTopic);
	}

    private void scheduleRetrySubscriptions() {
    	taskScheduler.scheduleAtFixedRate(this::initTopicSubscriptions, Instant.now().plusSeconds(reSubscriptionIntervalSecs),
				Duration.ofSeconds(reSubscriptionIntervalSecs));		
	}

	public void authTransactionTopicSubscription() {
    	logger.debug("subscribe", "",
                "Trying to subscribe to topic: " + authTransactionTopic + " callback-url: "
                        + authTransactionCallbackUrl);
        subscribe(authTransactionTopic, authTransactionCallbackUrl, authTransactionSecret, hubUrl);
        logger.info("subscribe", "",
                "Subscribed to topic: " + authTransactionTopic);
    }
	
	public void credentialStatusUpdateTopicSubscription() {
    	logger.debug("subscribe", "",
                "Trying to subscribe to topic: " + credentialStatusUpdateTopic + " callback-url: "
                        + credentialStatusUpdateCallbackUrl);
        subscribe(credentialStatusUpdateTopic, credentialStatusUpdateCallbackUrl, credentialStatusUpdateSecret, hubUrl);
        logger.info("subscribe", "",
                "Subscribed to topic: " + credentialStatusUpdateTopic);
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

    protected void authTypStatusTopicSubsriptions() {
        logger.debug("subscribe", "",
                "Trying to subscribe to topic: " + autTypeStatusTopic + " callback-url: "
                        + authTypeStatusCallbackUrl);
        subscribe(autTypeStatusTopic, authTypeStatusCallbackUrl, authTypeStatusSecret, hubUrl);
        logger.info("subscribe", "",
                "Subscribed to topic: " + autTypeStatusTopic);

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
