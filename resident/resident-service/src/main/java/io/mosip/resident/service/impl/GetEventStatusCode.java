package io.mosip.resident.service.impl;

import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusCanceled;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.util.GetEventStatusBasedOnLangCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class GetEventStatusCode {

    @Autowired
    private GetEventStatusBasedOnLangCode getEventStatusBasedOnLangCode;

    public Tuple2<String, String> getEventStatusCode(String statusCode, String langCode) {
        EventStatus status;
        if (EventStatusSuccess.containsStatus(statusCode)) {
            status = EventStatus.SUCCESS;
        } else if (EventStatusFailure.containsStatus(statusCode)) {
            status = EventStatus.FAILED;
        } else if(EventStatusCanceled.containsStatus(statusCode)){
            status = EventStatus.CANCELED;
        }
        else {
            status = EventStatus.IN_PROGRESS;
        }
        String fileText = getEventStatusBasedOnLangCode.getEventStatusBasedOnLangcode(status, langCode);
        return Tuples.of(status.name(), fileText);
    }
}
