package io.mosip.resident.mock.service;

import java.io.IOException;

import io.mosip.resident.exception.ApisResourceAccessException;

public interface MockService {
    byte[] getRIDDigitalCardV2(String rid) throws ApisResourceAccessException, IOException;
}
