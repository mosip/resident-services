package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.util.List;
import java.util.Map;

import com.ibm.icu.impl.UCharacterProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.preregistration.application.dto.TransliterationRequestDTO;
import io.mosip.preregistration.application.dto.TransliterationResponseDTO;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.validator.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This class provides different API's to perform operations on
 * Transliteration Application
 *
 * @author Kamesh Shekhar Prasad
 *
 */
@RestController
@RequestMapping("/transliteration")
@Tag(name = "Transliteration Controller", description = "Transliteration Controller")
public class TransliterationController {


	/** Autowired reference for {@link #transliterationService}. */
	@Autowired
	private TransliterationService transliterationService;

	@Autowired
	private RequestValidator requestValidator;
	
	@Autowired
	private AuditUtil audit;

	@Autowired
	private Environment environment;

	private static final String RESIDENT_TRANSLITERATION_WORKAROUND_PROPERTY = "resident-transliteration-workaround-for-%s-%s";
	private static final int LANGUAGE_LIST_SIZE = 2;


	/**
	 * Post API to transliterate from transliteration application.
	 *
	 * @param requestDTO
	 * @return responseDto with transliterated toFieldValue.
	 */
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping(path = "/transliterate", consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary  = "Get Pre-Registartion-Translitration data", description = "Get Pre-Registartion-Translitration data", tags = "Transliteration Controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Given key is translitrated successfully"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<MainResponseDTO<TransliterationResponseDTO>> translitrator(
			@Validated @RequestBody() MainRequestDTO<TransliterationRequestDTO> requestDTO) {
		try {
			requestValidator.validateId(requestDTO);
		} catch (InvalidInputException e) {
			audit.setAuditRequestDto(EventEnum.TRANSLITERATION_FAILURE);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, ResidentConstants.TRANSLITERATE_ID));
			throw e;
		}
		String propertyValue = environment.getProperty(String.format(RESIDENT_TRANSLITERATION_WORKAROUND_PROPERTY,
				requestDTO.getRequest().getFromFieldLang(), requestDTO.getRequest().getToFieldLang()));
		if (propertyValue != null) {
			List<String> propertyValueList = List.of(propertyValue.split(ResidentConstants.COMMA));
			MainResponseDTO<TransliterationResponseDTO> responseDTO = null;
			for(String languagePair:propertyValueList){
				MainRequestDTO<TransliterationRequestDTO> transliterationRequestDTOMainRequestDTO = new MainRequestDTO<>();
				TransliterationRequestDTO transliterationRequestDTO = new TransliterationRequestDTO();
				List<String> languageList = List.of(languagePair.split(ResidentConstants.HYPHEN));
				if(languageList.size() == LANGUAGE_LIST_SIZE){
					transliterationRequestDTO.setFromFieldLang(languageList.get(0));
					transliterationRequestDTO.setToFieldLang(languageList.get(1));
					if(responseDTO!=null){
						transliterationRequestDTO.setFromFieldValue(responseDTO.getResponse().getToFieldValue());
					} else {
						transliterationRequestDTO.setFromFieldValue(requestDTO.getRequest().getFromFieldValue());
					}
					transliterationRequestDTOMainRequestDTO.setRequest(transliterationRequestDTO);
					transliterationRequestDTOMainRequestDTO.setId(requestDTO.getId());
					transliterationRequestDTOMainRequestDTO.setVersion(requestDTO.getVersion());
					transliterationRequestDTOMainRequestDTO.setRequesttime(requestDTO.getRequesttime());
					responseDTO = transliterationService.translitratorService(transliterationRequestDTOMainRequestDTO);
				}
			}
			TransliterationResponseDTO transliterationResponseDTO = responseDTO.getResponse();
			transliterationResponseDTO.setToFieldLang(requestDTO.getRequest().getToFieldLang());
			transliterationResponseDTO.setFromFieldValue(requestDTO.getRequest().getFromFieldValue());
			transliterationResponseDTO.setFromFieldLang(requestDTO.getRequest().getFromFieldLang());
			responseDTO.setResponse(transliterationResponseDTO);
			return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(transliterationService.translitratorService(requestDTO));
		}
	}
}
