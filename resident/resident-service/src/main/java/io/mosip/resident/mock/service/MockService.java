package io.mosip.resident.mock.service;

import io.mosip.resident.exception.ApisResourceAccessException;

import java.io.IOException;

public interface MockService {
    byte[] getRIDDigitalCardV2(String rid) throws ApisResourceAccessException, IOException;
}
