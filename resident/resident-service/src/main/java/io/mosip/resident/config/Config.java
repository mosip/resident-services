package io.mosip.resident.config;

import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.templatemanager.velocity.impl.TemplateManagerImpl;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.servlet.Filter;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;


@Configuration
public class Config {
    private String defaultEncoding = StandardCharsets.UTF_8.name();
    /**
     * The resource loader.
     */
    private String resourceLoader = "classpath";

    /**
     * The template path.
     */
    private String templatePath = ".";

    /**
     * The cache.
     */
    private boolean cache = Boolean.TRUE;


    @Bean
    public FilterRegistrationBean<Filter> registerReqResFilter() {
        FilterRegistrationBean<Filter> corsBean = new FilterRegistrationBean<>();
        corsBean.setFilter(getReqResFilter());
        corsBean.setOrder(1);
        return corsBean;
    }

    @Bean
    @Primary
    public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext)).build());
        return new RestTemplate(requestFactory);
    }

    @Bean
    public Filter getReqResFilter() {
        return new ReqResFilter();
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator();
    }

    @Bean
    public TemplateManager getTemplateManager() {
        final Properties properties = new Properties();
        properties.put(RuntimeConstants.INPUT_ENCODING, defaultEncoding);
        properties.put(RuntimeConstants.OUTPUT_ENCODING, defaultEncoding);
        properties.put(RuntimeConstants.ENCODING_DEFAULT, defaultEncoding);
        properties.put(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
        properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatePath);
        properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, cache);
        properties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
        properties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        properties.put("file.resource.loader.class", FileResourceLoader.class.getName());
        VelocityEngine engine = new VelocityEngine(properties);
        engine.init();
        return new TemplateManagerImpl(engine);
    }
}