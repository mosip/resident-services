package io.mosip.resident.function;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to throw exception.
 */
public class RunnableException {
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws ApisResourceAccessException, ResidentServiceCheckedException;
    }
}
