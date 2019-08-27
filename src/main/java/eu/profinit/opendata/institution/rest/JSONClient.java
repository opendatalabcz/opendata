package eu.profinit.opendata.institution.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dm on 11/28/15.
 */
@Component
public class JSONClient {

    @Value("${mocr.json.api.protocol}")
    private String jsonApiProtocol;

    private RestTemplate restTemplate;

    private Logger log = LogManager.getLogger(JSONClient.class);

    @PostConstruct
    public void init() {
        SSLContext context = null;
        try {
            context = SSLContext.getInstance(jsonApiProtocol);
            context.init(null, null, null);
        } catch (NoSuchAlgorithmException|KeyManagementException e) {
            log.error("Cannot initialize security protocol context", e);
        }

        CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLContext(context)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(factory);
    }

    public JSONPackageList getPackageList(String apiUrl, String packagesPath, String packageListIdentifier) {
        try {
            URI uri = URI.create(apiUrl + packagesPath + "?id=" + packageListIdentifier);
            log.debug("Downloading package list from " + uri.toString());
            return restTemplate.getForObject(uri, JSONPackageList.class);
        } catch (RestClientException e) {
            log.error("Could not retreive package list", e);
            return null;
        }
    }

    public JSONPackageListStrict getPackageListStrict(String apiUrl, String packagesPath, String packageListIdentifier) {
        try {
            URI uri = URI.create(apiUrl + packagesPath + "?id=" + packageListIdentifier);
            log.debug("Downloading package list from " + uri.toString());
            return restTemplate.getForObject(uri, JSONPackageListStrict.class);
        } catch (RestClientException e) {
            log.error("Could not retreive package list", e);
            return null;
        }
    }

    public boolean checkUrlOK(String apiUrl, String packagesPath, String packageListIdentifier) {
        try {
            URI uri = URI.create(apiUrl + packagesPath + "?id=" + packageListIdentifier);
            log.debug("Checking url of " + uri.toString());
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            return response.getStatusCode().equals(HttpStatus.OK);
        } catch (RestClientException e) {
            log.error("Problem with retrieval.", e);
            return false;
        }
    }
}
