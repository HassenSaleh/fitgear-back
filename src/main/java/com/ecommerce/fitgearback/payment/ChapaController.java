package com.ecommerce.fitgearback.payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.SecondaryRow;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@PreAuthorize(value = "hasRole('CUSTOMER')")
@Getter
@SecondaryRow
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class ChapaController {
    private ChapaService chapaService;
    @PostMapping("/initialize")
    public String initializePayment(@RequestBody PaymentRequest request ) throws Exception {
        return chapaService.initializePayment(
                request.getAmount(),
                request.getCurrency(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getTxRef(),
                request.getCallbackUrl()
        );
    }

    @GetMapping("/verify/{txRef}")
    public boolean verifyPayment(@PathVariable String txRef) throws JsonProcessingException {
        return chapaService.verifyPayment(txRef);
    }

    @GetMapping("/callback")
    public void handleCallback(@RequestBody Map<String , Object> callbackData) {

        String txRef = (String) callbackData.get("txRef");
        String status = (String) callbackData.get("status");
        System.out.println("Callback received for txRef: " + txRef + " status: " + status);
    }
}
