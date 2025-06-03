package com.ecommerce.fitgearback.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Service
public class ChapaService {
    private static final Logger logger = LoggerFactory.getLogger(ChapaService.class);

    @Value("${chapa.secret-key}")
    private String secretKey;
    @Value("${chapa.public-key}")
    private String publicKey;
    @Value("${chapa.api-url}")
    private String apiUrl;

    @Value("${app.callback-url}")
    private String callbackUrl;
    @Value("${app.return-url}")
    private String returnUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    @Autowired
    public ChapaService(RestTemplate restTemplate, ObjectMapper objectMapper, OkHttpClient client) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.client = client;
        logger.info("Chapa API Key initialized: {}", secretKey != null ? "Set" : "Not set");
    }

    public ChapaResponse initializePayment(ChapaRequest paymentRequest) throws IOException {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("Chapa secret key is not configured");
            throw new IllegalStateException("Chapa secret key is not configured");
        }

        String jsonBody = objectMapper.writeValueAsString(paymentRequest);
        logger.debug("Request JSON: {}", jsonBody);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonBody);

        Request request = new Request.Builder()
                .url(apiUrl)
                .method("POST", body)
                .addHeader("Authorization", "Bearer " + secretKey)
                .addHeader("Content-Type", "application/json")
                .build();

        logger.info("Sending request to Chapa API: {}", apiUrl);
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            logger.info("Chapa API response: [Code: {}] {}", response.code(), responseBody);
            if (!response.isSuccessful()) {
                try {
                    // Parse error response to extract details
                    Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
                    String errorMessage = errorResponse.get("message") != null ? errorResponse.get("message").toString() : "Unknown error";
                    throw new IOException ("Chapa API error: HTTP " + response.code() + " - " + errorMessage);
                } catch (JsonProcessingException e) {
                    throw new IOException("Chapa API error: HTTP " + response.code() + " - Failed to parse response: " + responseBody, e);
                }
            }
            return objectMapper.readValue(responseBody, ChapaResponse.class);
        }
    }

    public boolean  verifyPayment(String txRef) throws IOException {
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
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
            boolean success = "success".equals(responseBody.get("status"));
            logger.info("Payment verification for txRef {}: {}", txRef, success ? "Successful" : "Failed");
            return success;
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse verification response for txRef {}: {}", txRef, e.getMessage());
            throw new IOException("Failed to parse verification response", e);
        }
    }
}
