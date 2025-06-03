package com.ecommerce.fitgearback.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Map;

@Data
public class ChapaRequest {
    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "^[0-9]+(\\.[0-9]{1,2})?$", message = "Amount must be a valid number")
    private String amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    private String currency;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Transaction reference is required")
    @JsonProperty("tx_ref")
    private String txRef;

    @NotBlank(message = "Callback URL is required")
    @JsonProperty("callback_url")
    private String callbackUrl;

    @NotBlank(message = "Return URL is required")
    @JsonProperty("return_url")
    private String returnUrl;

    private Map<String, String> customization;
    private Map<String, String> meta;

    private Long orderId; // Add orderId to link payment to order

    public void setCustomization(Map<String, String> customization) {
        if (customization != null && customization.get("title") != null && customization.get("title").length() > 16) {
            throw new IllegalArgumentException("customization.title must not exceed 16 characters");
        }
        this.customization = customization;
    }
}
