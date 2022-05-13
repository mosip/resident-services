package io.mosip.resident.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.model.Event;
import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Tag(name="WebSubUpdateAuthTypeController", description="WebSubUpdateAuthTypeController")
public class WebSubUpdateAuthTypeController {

    private static Logger logger = LoggerConfiguration.logConfig(WebSubUpdateAuthTypeController.class);

    @Autowired
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    /** The publisher. */
    @Autowired
    private PublisherClient<String, Object, HttpHeaders> publisher;

    @Autowired
    private WebSubUpdateAuthTypeService webSubUpdateAuthTypeService;

    @PostMapping(value = "/callback/authTypeCallback/{partnerId}/", consumes = "application/json")
    @Operation(summary = "WebSubUpdateAuthTypeController", description = "WebSubUpdateAuthTypeController",
            tags = {"WebSubUpdateAuthTypeController"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})

    //@PreAuthenticateContentAndVerifyIntent(secret = "abc123", callback = "http://localhost:8099/resident/v1/callback/authTypeCallback/AUTH_TYPE_STATUS_UPDATE_ACK", topic = "AUTH_TYPE_STATUS_UPDATE_ACK")
    public void authTypeCallback(@RequestBody EventModel eventModel, @PathVariable("partnerId") String partnerId) {
        logger.info("WebSubUpdateAuthTypeController");
        webSubUpdateAuthTypeService.updateAuthTypeStatus("1234","1234");

        //publisher.registerTopic("AUTH_TYPE_STATUS_UPDATE_ACK","http://localhost:9191/websub/publish");

        EventModel e1=new EventModel();
        Event event=new Event();
        event.setTransactionId("1234");

        e1.setEvent(event);
        e1.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
        e1.setPublishedOn(String.valueOf(LocalDateTime.now()));
        e1.setPublisher("AUTH_TYPE_STATUS_UPDATE_ACK");

        publisher.publishUpdate("AUTH_TYPE_STATUS_UPDATE_ACK", e1, MediaType.APPLICATION_JSON_VALUE, null, "http://localhost:9191/websub/publish");

        SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
        subscriptionRequest.setCallbackURL("http://localhost:8099/resident/v1/callback/authTypeCallback/AUTH_TYPE_STATUS_UPDATE_ACK");
        subscriptionRequest.setSecret("abc123");
        subscriptionRequest.setTopic("AUTH_TYPE_STATUS_UPDATE_ACK");
        subscriptionRequest.setHubURL("http://localhost:9191/websub/hub");
        subscribe.subscribe(subscriptionRequest);
    }

//    private AuthTransactionStatusEvent createEvent(String transactionId,  LocalDateTime updatedTimestamp) {
//        AuthTransactionStatusEvent event = new AuthTransactionStatusEvent();
//        event.setTransactionId(transactionId);
//        event.setTimestamp(updatedTimestamp);
//        return event;
//    }
}
