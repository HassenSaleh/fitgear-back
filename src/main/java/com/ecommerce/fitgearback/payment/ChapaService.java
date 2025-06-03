package com.ecommerce.fitgearback.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Service
public class ChapaService {
    private static final Logger logger = LoggerFactory.getLogger(ChapaService.class);

    @Value("${chapa.secret-key}")
    private String secretKey;
    @Value("${chapa.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ChapaService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        logger.info("ChapaService initialized");
    }

    public ChapaResponse initializePayment(ChapaRequest paymentRequest) throws IOException {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("Chapa secret key is not configured");
            throw new IllegalStateException("Chapa secret key is not configured");
        }

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(paymentRequest);
            logger.debug("Request JSON: {}", jsonBody);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize payment request: {}", e.getMessage());
            throw new IOException("Failed to serialize payment request", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        logger.info("Sending payment initialization request to Chapa API: {}", apiUrl);
        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            logger.info("Chapa API response: [Code: {}] {}", response.getStatusCodeValue(), responseBody);

            if (!response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
                String errorMessage = errorResponse.getOrDefault("message", "Unknown error").toString();
                throw new IOException("Chapa API error: HTTP " + response.getStatusCodeValue() + " - " + errorMessage);
            }

            return objectMapper.readValue(responseBody, ChapaResponse.class);
        } catch (RestClientException e) {
            logger.error("Chapa API request failed: {}", e.getMessage());
            throw new IOException("Chapa API request failed: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse Chapa API response: {}", e.getMessage());
            throw new IOException("Failed to parse Chapa API response", e);
        }
    }

    public boolean verifyPayment(String txRef) throws IOException {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("Chapa secret key is not configured");
            throw new IllegalStateException("Chapa secret key is not configured");
        }

        String url = apiUrl.replace("/transaction/initialize", "/transaction/verify/" + txRef);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + secretKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        logger.info("Verifying payment for txRef: {}", txRef);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            boolean success = "success".equalsIgnoreCase(responseBody.getOrDefault("status", "").toString());
            logger.info("Payment verification for txRef {}: {}", txRef, success ? "Successful" : "Failed");
            return success;
        } catch (RestClientException e) {
            logger.error("Payment verification failed for txRef {}: {}", txRef, e.getMessage());
            throw new IOException("Payment verification failed: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse verification response for txRef {}: {}", txRef, e.getMessage());
            throw new IOException("Failed to parse verification response", e);
        }
    }
}