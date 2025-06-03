package com.ecommerce.fitgearback.dtos.cart;

import com.ecommerce.fitgearback.repositories.CartRepository;
import com.ecommerce.fitgearback.services.CartService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Getter
@Setter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request){
        var response = cartService.getCartWithItems(request.getCartId());
        if(response != null){
            return ResponseEntity.ok(response);
        }
        return null;
        //return ResponseEntity.notFound().build();
    }
}
