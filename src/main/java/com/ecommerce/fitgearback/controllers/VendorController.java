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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/vendors")
public class VendorController {
    private final VendorService vendorService;


    @PostMapping
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

//    @PostMapping("/login")
//    public ResponseEntity<VendorResponseDto> loginVendor(@Valid @RequestBody VendorDto dto) {
//        VendorResponseDto responseDto = vendorService.loginVendor(dto);
//        if (responseDto != null) {
//            return ResponseEntity.ok(responseDto);
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponseDto> getVendorById(@PathVariable Long id) {
        VendorResponseDto vendor = vendorService.getVendorById(id);
        if(vendor != null ){
            return ResponseEntity.ok(vendor);
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping
    public ResponseEntity<Iterable<VendorResponseDto>> getAllVendors() {
        Iterable<VendorResponseDto> vendors = vendorService.getAllVendors();
        if(vendors.iterator().hasNext()){
            return ResponseEntity.ok(vendors);
        }
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }


//    @PostMapping("/login")
//    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
//        LoginResponseDto response = vendorService.login(loginRequest);
//        return ResponseEntity.ok(response);
//    }
}
