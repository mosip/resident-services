package io.mosip.resident.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.AcknowledgementService;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;

/**
 * This class is used to create service class implementation for getting acknowledgement API.
 * @Author Kamesh Shekhar Prasad
 */
@Service
public class AcknowledgementServiceImpl implements AcknowledgementService {

    private static final Logger logger = LoggerConfiguration.logConfig(AcknowledgementServiceImpl.class);

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private ProxyMasterdataServiceImpl proxyMasterdataServiceImpl;

    @Autowired
    private TemplateUtil templateUtil;

    private static final String CLASSPATH = "classpath";
    private static final String ENCODE_TYPE = "UTF-8";

    private TemplateManager templateManager;

    @Autowired
    private TemplateManagerBuilder templateManagerBuilder;

    @PostConstruct
    public void idTemplateManagerPostConstruct() {
        templateManager = templateManagerBuilder.encodingType(ENCODE_TYPE).enableCache(false).resourceLoader(CLASSPATH)
                .build();
    }

    @Autowired
    private Utility utility;

    @Override
    public byte[] getAcknowledgementPDF(String eventId, String languageCode, int timeZoneOffset) throws ResidentServiceCheckedException, IOException {
        logger.debug("AcknowledgementServiceImpl::getAcknowledgementPDF()::entry");

            Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository
                    .findById(eventId);
            String requestTypeCode;
            if (residentTransactionEntity.isPresent()) {
                requestTypeCode = residentTransactionEntity.get().getRequestTypeCode();
            } else {
                throw new ResidentServiceCheckedException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND);
            }
            Tuple2<Map<String, String>, String> ackTemplateVariables = RequestType.valueOf(requestTypeCode).getAckTemplateVariables(templateUtil, eventId, languageCode, timeZoneOffset);
			String requestProperty = ackTemplateVariables.getT2();
            ResponseWrapper<?> responseWrapper = proxyMasterdataServiceImpl.
                    getAllTemplateBylangCodeAndTemplateTypeCode(languageCode, requestProperty);
            Map<String, Object> templateResponse = new LinkedHashMap<>((Map<String, Object>) responseWrapper.getResponse());
//            String fileText = (String) templateResponse.get(ResidentConstants.FILE_TEXT);
            String fileText = "<html><head>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html;\" charset=\"windows-1252\">\n" +
                    "<meta name=\"Generator\" content=\"Microsoft\" word=\"\" 15=\"\" (filtered)=\"\">\n" +
                    "</head><body lang=\"EN-US\">\n" +
                    "\n" +
                    "<div style=\"width:100%;height:100vh;\">\n" +
                    "\t<table style=\"width: 339px;margin: 0 auto;background: #FFFFFF;border-radius: 10px;opacity: 1;border:1px solid #E2E2E2;\">\n" +
                    "<tbody><tr>\n" +
                    "#if($isPhotoSet)\n" +
                    "<td style=\"width:25%;vertical-align: top;padding:5px;\"><img style=\"top: 51px;left: 45px;width: 80px;height: 93px;opacity: 1;border:1px solid #E2E2E2;border-radius:5px;\" src=$!ApplicantPhoto></td>\n" +
                    "#end\n" +
                    "<td style=\"width:65%\">\n" +
                    "\t\t<table style=\"border:none;border-collapse: collapse;width: 100%;\">\n" +
                    "\t\t  <tbody><tr>\n" +
                    "\t\t\t<td colspan=\"2\" style=\"padding:3px;\"><label style=\"text-align: left;font: normal normal normal 11px/14px Inter;letter-spacing: 0px;color: #666666;opacity: 1;padding: 3px;\">Full Name</label>\n" +
                    "            <div colspan=\"2\" style=\"/* font-weight: bold; */text-align: left;font: normal normal 600 12px/15px Inter;letter-spacing: 0px;color: #000000;opacity: 1;padding:0px 3px;\">$name</div></td>\n" +
                    "\t\t  </tr>\n" +
                    "\t\t  \n" +
                    "\t\t  <tr>\n" +
                    "\t\t\t<td style=\"padding:3px;\"><label style=\"text-align: left;font: normal normal normal 11px/14px Inter;letter-spacing: 0px;color: #666666;opacity: 1;padding: 3px;\">DoB</label>\n" +
                    "            <div colspan=\"2\" style=\"text-align: left;font: normal normal 600 12px/15px Inter;letter-spacing: 0px;color: #000000;opacity: 1;padding:0px 3px;\">$dateOfBirth</div></td>\n" +
                    "\t\t\t<td style=\"padding:3px;\"><label style=\"text-align: left;font: normal normal normal 11px/14px Inter;letter-spacing: 0px;color: #666666;opacity: 1;\">ID Type</label><div colspan=\"2\" style=\"text-align: left;font: normal normal 600 12px/15px Inter;letter-spacing: 0px;color: #000000;opacity: 1;/* padding: 0px 0px; */\">VID</div></td>\n" +
                    "\t\t  </tr>\n" +
                    "\t\t\n" +
                    "\t\t</tbody></table>\n" +
                    "\t</td>\n" +
                    "\t<td style=\"width: 15%;vertical-align:top;/* margin-top: 10%; */\"><img style=\"/* top: 46px; *//* left: 324px; */width: 35px;height: 38px;background: transparent url('img/logo (2).png') 0% 0% no-repeat padding-box;opacity: 1;\" src=\"https://mosip.io/images/logo.png\" alt=\"mosip\"></td>\n" +
                    "</tr>\n" +
                    "<tr>\n" +
                    "\t<td colspan=\"6\">\n" +
                    "\t\t<table style=\"border:none;border-collapse: collapse;width: 100%;\">\n" +
                    "\t\t\t<tbody><tr>\n" +
                    "\t\t\t\n" +
                    "\n" +
                    "\t\t\t\t<td style=\"width: 50%;padding: 5px;\">\n" +
                    "\t\t\t\t<div colspan=\"12\">\n" +
                    "\t\t\t\t\t\t<div colspan=\"6\" >\n" +
                    "\t\t\t\t<label style=\"text-align: left;font: normal normal normal 11px/14px Inter;letter-spacing: 0px;color: #666666;opacity: 1;\">VID</label><div colspan=\"2\" style=\"text-align: left;font: normal normal 600 12px/15px Inter;letter-spacing: 0px;color: #000000;opacity: 1;\">$vid</div></div></td>\n" +
                    "\t\t\t\t\n" +
                    "\t\t\t\t<td style=\"width: 50%;padding: 5px;\"><div colspan=\"6\" style=\"padding-left: 32px;\" ><label style=\"text-align: left;font: normal normal normal 11px/14px Inter;letter-spacing: 0px;color: #666666;opacity: 1;\">VID Type</label><div colspan=\"2\" style=\"text-align: left;font: normal normal 600 12px/15px Inter;letter-spacing: 0px;color: #000000;opacity: 1;\">$vidType</div></div></td>\n" +
                    "\t\t\t\t</div>\n" +
                    "\t\t\t\t\n" +
                    "\t\t\t</tr>\n" +
                    "\t\t\n" +
                    "\t\t\t<tr>\n" +
                    "\t\t\t\t<td style=\"width: 50%;\">\n" +
                    "\t\t\t\t<div colspan=\"12\">\n" +
                    "\t\t\t\t<div colspan=\"6\" >\n" +
                    "\t\t\t\t<label style=\"text-align: left;font: normal normal normal 11px/14px Inter;letter-spacing: 0px;color: #666666;opacity: 1;\">Generated on</label><div colspan=\"2\" style=\"text-align: left;font: normal normal 600 12px/15px Inter;letter-spacing: 0px;color: #000000;opacity: 1;padding: 2px 1px;\">$genratedOnTimestamp</div></div></td>\n" +
                    "\t\t\t\t<td style=\"width: 50%;\"><div colspan=\"6\" style=\"padding-left: 32px;\" ><label style=\"text-align: left;font: normal normal normal 11px/14px Inter;letter-spacing: 0px;color: #666666;opacity: 1;\">Expires on</label><div colspan=\"2\" style=\"text-align: left;font: normal normal 600 12px/15px Inter;letter-spacing: 0px;color: #000000;opacity: 1;padding: 2px 2px;\">$expiryTimestamp</div></div></td>\n" +
                    "\t\t\t\t</div>\n" +
                    "\t\t\t</tr>\n" +
                    "\n" +
                    "\t\t</tbody></table>\n" +
                    "\t</td>\n" +
                    "</tr>\n" +
                    "<tr>\n" +
                    "\t<td colspan=\"3\" style=\"text-align: left;font: normal normal 600 12px/15px Inter;color: #000000;opacity: 1;height: 40px;text-align: center;border-top:1px solid #E2E2E2;\"><img style=\"top: 51px;left: 45px; margin-bottom: -4px;\" src=\"data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAABmJLR0QA/wD/AP+gvaeTAAAAgElEQVQokc3RwQ2CYAyG4YdEjgRhA2PigTGcQ5dhAHaBATw7gQdPLuAMeuA3KlFTb3xJD03ztl9b5qocLcp/wQLHFG/wDrdgDJAlsMbqx8QMHTbY4hS1usQBzadik4pVpNNiAp2xNu7yTRdcH0kvfpz9a5fS89xFxOoUbo3PnqHungkjNGCrVUcAAAAASUVORK5CYII=\"><span style=\"margin-left: 5px;\"> </span>Transactions allowed : $transactionsLeftCount</td>\n" +
                    "</tr>\n" +
                    "</tbody></table>\n" +
                    "</div>\n" +
                    "</body></html>";
            Map<String, String> templateVariables = ackTemplateVariables.getT1();
            InputStream stream = new ByteArrayInputStream(fileText.getBytes(StandardCharsets.UTF_8));
            InputStream templateValue = templateManager.merge(stream, convertMapValueFromStringToObject(templateVariables));
            logger.debug("AcknowledgementServiceImpl::getAcknowledgementPDF()::exit");
            return utility.signPdf(templateValue, null);

    }

    public Map<String, Object> convertMapValueFromStringToObject(Map<String, String> templateVariables) {
        Map<String, Object> templateMapObject = new HashMap<>();
        for(Map.Entry<String, String> entry: templateVariables.entrySet()){
            templateMapObject.put(entry.getKey(), entry.getValue());
        }
        return templateMapObject;
    }

}

