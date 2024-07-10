package io.mosip.resident.service.impl;

import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusCanceled;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.util.TemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Component
public class GetEventStatusCode {

    @Autowired
    private  TemplateUtil templateUtil;

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
        String fileText = templateUtil.getEventStatusBasedOnLangcode(status, langCode);
        return Tuples.of(status.name(), fileText);
    }
}
