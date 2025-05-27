package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.VendorDto;
import com.ecommerce.fitgearback.dtos.VendorResponseDto;
import com.ecommerce.fitgearback.services.VendorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/vendors")
public class VendorController {
    private final VendorService vendorService;


    @PostMapping("/register")
    public ResponseEntity<VendorResponseDto> registerVendor(@Valid @RequestBody VendorDto dto) {
        VendorResponseDto responseDto = vendorService.registerVendor(dto);
        if (responseDto != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/users/{id}")
                    .buildAndExpand(responseDto.getId())
                    .toUri();
            return ResponseEntity.created(location).body(responseDto);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
