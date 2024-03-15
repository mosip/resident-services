package io.mosip.resident.service.function;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * @author Kamesh Shekhar Prasad
 * This interface is used to throw exception.
 */
@FunctionalInterface
public interface RunnableWithException {
    void run() throws ApisResourceAccessException, ResidentServiceCheckedException;
}

