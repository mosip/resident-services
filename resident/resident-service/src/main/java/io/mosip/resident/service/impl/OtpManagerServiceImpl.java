package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.preregistration.application.constant.PreRegLoginErrorConstants;
import io.mosip.preregistration.application.dto.OTPGenerateRequestDTO;
import io.mosip.preregistration.application.dto.RequestDTO;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.ResidentUpdateRequestDto;
import io.mosip.resident.entity.OtpTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.OtpTransactionRepository;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.OtpManager;
import io.mosip.resident.service.ResidentService;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.validator.RequestValidator;
import reactor.util.function.Tuple2;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to generate otp and validate otp.
 */
@Service
public class OtpManagerServiceImpl implements OtpManager {

    private static final Logger logger = LoggerConfiguration.logConfig(OtpManagerServiceImpl.class);
    private static final String OTP_EXPIRED = "OTP_EXPIRED";
    private static final String ACCEPTED = "accepted";
    private static final String PHONE = "phone";
    private static final String EMAIL = "email";

    @Autowired
    private OtpTransactionRepository otpRepo;

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    RestTemplate restTemplate;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IdentityServiceImpl identityService;

    @Autowired
    private ResidentService residentService;

    @Autowired
    private TemplateUtil templateUtil;

    @Autowired
    private RequestValidator requestValidator;


    @Override
    public boolean sendOtp(MainRequestDTO<OtpRequestDTOV2> requestDTO, String channelType, String language) throws IOException, ResidentServiceCheckedException, ApisResourceAccessException {
        this.logger.info("sessionId", "idType", "id", "In sendOtp method of otpmanager service ");
        String userId = requestDTO.getRequest().getUserId();
        NotificationRequestDto notificationRequestDto = new NotificationRequestDtoV2();
        notificationRequestDto.setId(identityService.getResidentIndvidualId());
        String refId = this.hash(userId+requestDTO.getRequest().getTransactionId());
        if (this.otpRepo.checkotpsent(refId, "active", DateUtils.getUTCCurrentDateTime(), DateUtils.getUTCCurrentDateTime()
                .minusMinutes(this.environment.getProperty("otp.request.flooding.duration", Long.class))) >
        this.environment.getProperty("otp.request.flooding.max-count", Integer.class)) {
            this.logger.error("sessionId", this.getClass().getSimpleName(), ResidentErrorCode.OTP_REQUEST_FLOODED.getErrorCode(), "OTP_REQUEST_FLOODED");
            throw new ResidentServiceCheckedException(ResidentErrorCode.OTP_REQUEST_FLOODED.getErrorCode(), ResidentErrorCode.OTP_REQUEST_FLOODED.getErrorMessage());
        } else {
            String otp = this.generateOTP(requestDTO);
            this.logger.info("sessionId", "idType", "id", "In generateOTP method of otpmanager service OTP generated");
            String otpHash = digestAsPlainText((userId + this.environment.getProperty("mosip.kernel.data-key-splitter") + otp+
                    requestDTO.getRequest().getTransactionId()).getBytes());
            OtpTransactionEntity otpTxn;
            otpTxn = new OtpTransactionEntity();
            otpTxn.setId(UUID.randomUUID().toString());
            otpTxn.setRefId(this.hash(userId + requestDTO.getRequest().getTransactionId()));
            otpTxn.setOtpHash(otpHash);
            otpTxn.setCrBy(this.environment.getProperty("resident.clientId"));
            otpTxn.setCrDtimes(DateUtils.getUTCCurrentDateTime());
            otpTxn.setGeneratedDtimes(DateUtils.getUTCCurrentDateTime());
            otpTxn.setExpiryDtimes(DateUtils.getUTCCurrentDateTime().plusSeconds((Long)
                    this.environment.getProperty("mosip.kernel.otp.expiry-time", Long.class)));
            otpTxn.setStatusCode("active");
            this.otpRepo.save(otpTxn);
            if (channelType.equalsIgnoreCase("phone")) {
                this.logger.info("sessionId", "idType", "id", "In generateOTP method of otpmanager service invoking sms notification");
                NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
                notificationRequestDtoV2.setTemplateType(TemplateType.SUCCESS);
                notificationRequestDtoV2.setRequestType(RequestType.SEND_OTP);
                notificationRequestDtoV2.setOtp(otp);
                notificationService
                        .sendNotification(notificationRequestDto, List.of(channelType), null, userId);
            }

            if (channelType.equalsIgnoreCase("email")) {
                this.logger.info("sessionId", "idType", "id", "In generateOTP method of otpmanager service invoking email notification");
                NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
                notificationRequestDtoV2.setTemplateType(TemplateType.SUCCESS);
                notificationRequestDtoV2.setRequestType(RequestType.SEND_OTP);
                notificationRequestDtoV2.setOtp(otp);
                notificationService
                        .sendNotification(notificationRequestDto, List.of(channelType), userId, null);
            }

            return true;
        }
    }

    private String generateOTP(MainRequestDTO<OtpRequestDTOV2> requestDTO) {
        this.logger.info("sessionId", "idType", "id", "In generateOTP method of otpmanager service ");

        try {
            OTPGenerateRequestDTO otpRequestDTO = new OTPGenerateRequestDTO();
            otpRequestDTO.setId(requestDTO.getId());
            otpRequestDTO.setRequesttime(requestDTO.getRequesttime());
            otpRequestDTO.setVersion(requestDTO.getVersion());
            RequestDTO req = new RequestDTO();
            req.setKey(requestDTO.getRequest().getUserId());
            otpRequestDTO.setRequest(req);
            HttpHeaders headers1 = new HttpHeaders();
            headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers1.setContentType(MediaType.APPLICATION_JSON_UTF8);
            headers1.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            HttpEntity<OTPGenerateRequestDTO> entity1 = new HttpEntity(otpRequestDTO, headers1);
            ResponseWrapper<Map<String, String>> response = (ResponseWrapper)this.restTemplate.exchange(this.environment.getProperty("otp-generate.rest.uri"), HttpMethod.POST, entity1, ResponseWrapper.class, new Object[0]).getBody();
            String otp = null;
            if (response != null) {
                Map<String, String> res = (Map)response.getResponse();
                if (res != null) {
                    if (((String)res.get("status")).equals("USER_BLOCKED")) {
                        this.logger.error("sessionId", this.getClass().getSimpleName(), ResidentErrorCode.BLOCKED_OTP_VALIDATE.getErrorCode(), "USER_BLOCKED");
                        throw new ResidentServiceException(ResidentErrorCode.BLOCKED_OTP_VALIDATE.getErrorCode(), ResidentErrorCode.BLOCKED_OTP_VALIDATE.getErrorMessage());
                    }

                    otp = res.get("otp");
                }
            }

            return otp;
        } catch (ResidentServiceException var9) {
            this.logger.error("sessionId", this.getClass().getSimpleName(), "generateOTP", var9.getMessage());
            throw new ResidentServiceException(ResidentErrorCode.UNABLE_TO_PROCESS.getErrorCode(), ResidentErrorCode.UNABLE_TO_PROCESS.getErrorMessage());
        } catch (Exception var10) {
            this.logger.error("sessionId", this.getClass().getSimpleName(), ResidentErrorCode.SERVER_ERROR.getErrorCode(), ResidentErrorCode.SERVER_ERROR.getErrorMessage());
            throw new ResidentServiceException(ResidentErrorCode.SERVER_ERROR.getErrorCode(), ResidentErrorCode.SERVER_ERROR.getErrorMessage());
        }
    }

    @Override
    public boolean 	validateOtp(String otp, String userId, String transactionId) throws ApisResourceAccessException, ResidentServiceCheckedException {
        logger.info("sessionId", "idType", "id", "In validateOtp method of otpmanager service ");
        String otpHash;
        otpHash = digestAsPlainText(
                (userId + this.environment.getProperty("mosip.kernel.data-key-splitter") + otp+transactionId).getBytes());

        if (!otpRepo.existsByOtpHashAndStatusCode(otpHash, PreRegLoginConstant.ACTIVE_STATUS)) {
            return false;
        }
        
        OtpTransactionEntity otpTxn = otpRepo.findTopByOtpHashAndStatusCode(otpHash, PreRegLoginConstant.ACTIVE_STATUS);
        otpTxn.setStatusCode(PreRegLoginConstant.USED_STATUS);
        otpRepo.save(otpTxn);
        if (!(otpTxn.getExpiryDtimes().isAfter(DateUtils.getUTCCurrentDateTime()))) {
            logger.error(PreRegLoginConstant.SESSION_ID, this.getClass().getSimpleName(),
                    PreRegLoginErrorConstants.EXPIRED_OTP.getErrorCode(), OTP_EXPIRED);
            throw new ResidentServiceException(ResidentErrorCode.EXPIRED_OTP.getErrorCode(),
                    ResidentErrorCode.EXPIRED_OTP.getErrorMessage());
        }
        return true;
    }

    public Tuple2<Object, String> updateUserId(String userId, String transactionId) throws ApisResourceAccessException, ResidentServiceCheckedException {
        ResidentUpdateRequestDto residentUpdateRequestDto = new ResidentUpdateRequestDto();
        String individualId= identityService.getResidentIndvidualId();
        String individualIdType = templateUtil.getIndividualIdType();
        residentUpdateRequestDto.setIndividualId(individualId);
        residentUpdateRequestDto.setConsent(ACCEPTED);
        residentUpdateRequestDto.setIdentityJson(getIdentityJson(individualId, transactionId, userId, individualIdType));
        residentUpdateRequestDto.setIndividualIdType(individualIdType);
        return residentService.reqUinUpdate(residentUpdateRequestDto);
    }

    public String getIdentityJson(String individualId, String transactionId, String userId, String individualIdType) {
        Map identityMap = new LinkedHashMap();
        identityMap.put("IDSchemaVersion", "0.1");
        identityMap.put(individualIdType, individualId);
        String channel = getChannel(userId, transactionId);
        identityMap.put(channel, userId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("identity", identityMap);
        String str = CryptoUtil.encodeToPlainBase64(jsonObject.toString().getBytes());
        return String.valueOf(str);
    }

    public String getChannel(String userId, String transactionId) {
        List<String> channels = requestValidator.validateUserIdAndTransactionId(userId, transactionId);
        if(channels.get(0).equalsIgnoreCase(PHONE)){
            return PHONE;
        } else{
            return EMAIL;
        }
    }

    public static String digestAsPlainText(byte[] data) {
        return DatatypeConverter.printHexBinary(data).toUpperCase();
    }

    public String hash(String id) throws ResidentServiceException {
        return HMACUtils.digestAsPlainText(id.getBytes());
    }
}
