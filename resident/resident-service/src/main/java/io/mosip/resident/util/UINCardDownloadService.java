package io.mosip.resident.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.PrintRequest;
import io.mosip.resident.dto.UINCardRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;

@Component
public class UINCardDownloadService {
	private static final Logger logger = LoggerConfiguration.logConfig(UINCardDownloadService.class);
	@Autowired
    private Environment env;

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    private static final String PRINT_ID="mosip.registration.processor.print.id";
    private static final String PRINT_VERSION="mosip.registration.processor.application.version";
    private static final String ERRORS="errors";
    
    public byte[] getUINCard(String individualId,String cardType,IdType idType) throws ApisResourceAccessException {
    	PrintRequest request=new PrintRequest();
		UINCardRequestDTO uincardDTO=new UINCardRequestDTO();
		uincardDTO.setCardType(cardType);
		uincardDTO.setIdValue(individualId);
		uincardDTO.setIdtype(idType);
		request.setRequest(uincardDTO);
		request.setId(env.getProperty(PRINT_ID));
		request.setVersion(env.getProperty(PRINT_VERSION));
		request.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
		byte[]	response;
		try {
			response = (byte[]) residentServiceRestClient.postApi(env.getProperty(ApiName.REGPROCPRINT.name()),
					null, request, byte[].class);
			if(response ==null) {
				throw new ApisResourceAccessException();
			}
			String res= new String(response);
			if(res.contains(ERRORS)) {
				JSONObject responseJson=new JSONObject(res);
				JSONArray errorJson=responseJson.getJSONArray(ERRORS);
				for(int i=0;i<errorJson.length();i++) {
					if(!errorJson.getJSONObject(i).getString("message").isEmpty()) {
						throw new ApisResourceAccessException( errorJson.getJSONObject(i).getString("message"));
					}
				}
				
				
			} 
		
		} catch ( Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					idType.toString(), ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
					+ e.getMessage()+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Unable to fetch uin card -->"+e.getMessage());
		} 
		return response;
    }
}
