package com.ecommerce.fitgearback.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Service
public class ChapaService {
    @Value("${chapa.secret-key}")
    private String secretKey;
    @Value("${chapa.public-key}")
    private String publicKey;
    @Value("${chapa.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    public ChapaService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String initializePayment(String amount, String currency,String email,String firstName,String lastName,String txtRef,String callbackUrl) throws JsonProcessingException {
        String url = apiUrl + "/transaction/initialize";
        Map<String,Object> requestBody = new HashMap<>();

        requestBody.put("amount", amount);
        requestBody.put("currency", currency);
        requestBody.put("email", email);
        requestBody.put("firstName", firstName);
        requestBody.put("lastName", lastName);
//        requestBody.put("phone", phone);
        //requestBody.put("address", address);
//        requestBody.put("city", city);
//        requestBody.put("state", state);
        requestBody.put("txtRef", txtRef);
        requestBody.put("callback_url", callbackUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic "  + secretKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,entity,String.class);


        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(),Map.class);

        if("success".equals(responseBody.get("status"))){
            return (String) ((Map<?, ?>) responseBody.get("data")).get("payment_url");
        }else{
            throw new RuntimeException("Payment initialization failed: " + responseBody.get("message"));
        }
    }

    public boolean verifyPayment(String txtRef) throws JsonProcessingException {
        String url = apiUrl + "/transaction/verify";
        Map<String,Object> requestBody = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + secretKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,entity,String.class);

        Map<String , Object> responseBody = objectMapper.readValue(response.getBody(),Map.class);
        return "success".equals(responseBody.get("status"));
    }
}
