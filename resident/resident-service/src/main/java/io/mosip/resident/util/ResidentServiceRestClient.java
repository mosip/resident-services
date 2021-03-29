package io.mosip.resident.util;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.exception.ApisResourceAccessException;

/**
 * The Class RestApiClient.
 *
 * @author Monobikash Das
 */
@Component
public class ResidentServiceRestClient {

	/** The logger. */
	private final Logger logger = LoggerConfiguration.logConfig(ResidentServiceRestClient.class);

	/** The builder. */
	@Autowired
	RestTemplateBuilder builder;

	@Autowired
	private RestTemplate residentRestTemplate;

	@Autowired
	Environment environment;

	/**
	 * Gets the api.
	 *
	 * @param <T>
	 *            the generic type
	 * @param token
	 *            the token
	 * @param responseType
	 *            the response type
	 * @return the api
	 * @throws Exception
	 */
	public <T> T getApi(URI uri, Class<?> responseType, String token) throws ApisResourceAccessException {
		try {
			residentRestTemplate = getResidentRestTemplate();
			return (T) residentRestTemplate.exchange(uri, HttpMethod.GET, setRequestHeader(null, null, token), responseType)
					.getBody();
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Exception occured while accessing " + uri, e);
		}

	}

	public Object getApi(ApiName apiName, List<String> pathsegments, String queryParamName, String queryParamValue,
			Class<?> responseType, String token) throws ApisResourceAccessException {

		Object obj = null;
		String apiHostIpPort = environment.getProperty(apiName.name());
		UriComponentsBuilder builder = null;
		UriComponents uriComponents = null;
		if (apiHostIpPort != null) {
			builder = UriComponentsBuilder.fromUriString(apiHostIpPort);
			if (!((pathsegments == null) || (pathsegments.isEmpty()))) {
				for (String segment : pathsegments) {
					if (!((segment == null) || (("").equals(segment)))) {
						builder.pathSegment(segment);
					}
				}
			}

			if (StringUtils.isNotEmpty(queryParamName)) {

				String[] queryParamNameArr = queryParamName.split(",");
				String[] queryParamValueArr = queryParamValue.split(",");
				for (int i = 0; i < queryParamNameArr.length; i++) {
					builder.queryParam(queryParamNameArr[i], queryParamValueArr[i]);
				}

			}
			try {

				uriComponents = builder.build(false).encode();
				obj = getApi(uriComponents.toUri(), responseType, token);

			} catch (Exception e) {
				e.printStackTrace();
				throw new ApisResourceAccessException("Exception occured while accessing ", e);

			}
		}

		return obj;
	}


	public Object getApi(ApiName apiName, List<String> pathsegments, List<String> queryParamName, List<Object> queryParamValue,
						 Class<?> responseType, String token) throws ApisResourceAccessException {

		Object obj = null;
		String apiHostIpPort = environment.getProperty(apiName.name());
		UriComponentsBuilder builder = null;
		UriComponents uriComponents = null;
		if (apiHostIpPort != null) {
			builder = UriComponentsBuilder.fromUriString(apiHostIpPort);
			if (!((pathsegments == null) || (pathsegments.isEmpty()))) {
				for (String segment : pathsegments) {
					if (!((segment == null) || (("").equals(segment)))) {
						builder.pathSegment(segment);
					}
				}
			}

			if (!((queryParamName == null) || (("").equals(queryParamName)))) {

				for (int i = 0; i < queryParamName.size(); i++) {
					builder.queryParam(queryParamName.get(i), queryParamValue.get(i));
				}

			}
			try {

				uriComponents = builder.build(false).encode();
				obj = getApi(uriComponents.toUri(), responseType, token);

			} catch (Exception e) {
				e.printStackTrace();
				throw new ApisResourceAccessException("Exception occured while accessing ", e);

			}
		}

		return obj;
	}

	@SuppressWarnings({ "unchecked", "null" })
	public <T> T getApi(ApiName apiName, Map<String, String> pathsegments, Class<?> responseType, String token)
			throws Exception {

		String apiHostIpPort = environment.getProperty(apiName.name());
		Object obj = null;
		UriComponentsBuilder builder = null;
		if (apiHostIpPort != null) {

			builder = UriComponentsBuilder.fromUriString(apiHostIpPort);

			URI urlWithPath = builder.build(pathsegments);
			try {
				obj = getApi(urlWithPath, responseType, token);

			} catch (Exception e) {
				throw new Exception(e);
			}

		}
		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public <T> T postApi(String uri, MediaType mediaType, Object requestType, Class<?> responseClass, String token)
			throws ApisResourceAccessException {
		try {
			residentRestTemplate = getResidentRestTemplate();
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), uri);
			T response = (T) residentRestTemplate.postForObject(uri, setRequestHeader(requestType, mediaType, token),
					responseClass);
			return response;

		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw new ApisResourceAccessException("Exception occured while accessing " + uri, e);
		}
	}

	/**
	 * Patch api.
	 *
	 * @param <T>
	 *            the generic type
	 * @param uri
	 *            the uri
	 * @param requestType
	 *            the request type
	 * @param responseClass
	 *            the response class
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public <T> T patchApi(String uri, MediaType mediaType, Object requestType, Class<?> responseClass, String token)
			throws ApisResourceAccessException {

		T result = null;
		try {
			residentRestTemplate = getResidentRestTemplate();
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), uri);
			result = (T) residentRestTemplate.patchForObject(uri, setRequestHeader(requestType, mediaType, token),
					responseClass);

		} catch (Exception e) {

			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw new ApisResourceAccessException("Exception occured while accessing " + uri, e);
		}
		return result;
	}

	public <T> T patchApi(String uri, Object requestType, Class<?> responseClass, String token) throws Exception {
		return patchApi(uri, null, requestType, responseClass, token);
	}

	/**
	 * Put api.
	 *
	 * @param <T>
	 *            the generic type
	 * @param uri
	 *            the uri
	 * @param requestType
	 *            the request type
	 * @param responseClass
	 *            the response class
	 * @param mediaType
	 * @return the t
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T putApi(String uri, Object requestType, Class<?> responseClass, MediaType mediaType, String token)
			throws ApisResourceAccessException {

		T result = null;
		ResponseEntity<T> response = null;
		try {
			residentRestTemplate = getResidentRestTemplate();
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), uri);

			response = (ResponseEntity<T>) residentRestTemplate.exchange(uri, HttpMethod.PUT,
					setRequestHeader(requestType.toString(), mediaType, token), responseClass);
			result = response.getBody();
		} catch (Exception e) {

			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw new ApisResourceAccessException("Exception occured while accessing " + uri, e);
		}
		return result;
	}

	public RestTemplate getResidentRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), Arrays.asList(environment.getActiveProfiles()).toString());

		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		return new RestTemplate(requestFactory);

	}

	/**
	 * this method sets token to header of the request
	 *
	 * @param requestType
	 * @param mediaType
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private HttpEntity<Object> setRequestHeader(Object requestType, MediaType mediaType, String token)
			throws IOException {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Cookie", token);
		headers.add("Authorization", token);
		if (mediaType != null) {
			headers.add("Content-Type", mediaType.toString());

		}
		if (requestType != null) {
			try {
				HttpEntity<Object> httpEntity = (HttpEntity<Object>) requestType;
				HttpHeaders httpHeader = httpEntity.getHeaders();
				Iterator<String> iterator = httpHeader.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if (!(headers.containsKey("Content-Type") && key == "Content-Type"))
						headers.add(key, httpHeader.get(key).get(0));
				}
				return new HttpEntity<Object>(httpEntity.getBody(), headers);
			} catch (ClassCastException e) {
				return new HttpEntity<Object>(requestType, headers);
			}
		} else
			return new HttpEntity<Object>(headers);
	}

}
