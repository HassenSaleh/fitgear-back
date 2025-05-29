package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.vendor.*;
import com.ecommerce.fitgearback.services.VendorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/vendor")
@PreAuthorize(value = "hasRole('VENDOR')")

public class VendorController {
    private final VendorService vendorService;

    @GetMapping
    public ResponseEntity<Iterable<VendorResponse>> getAllVendors() {
        Iterable<VendorResponse> vendors = vendorService.getAllVendors();
        if(vendors.iterator().hasNext()){
            return ResponseEntity.ok(vendors);
        }
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Long id) {
        VendorResponse vendor = vendorService.getVendorById(id);
        if(vendor != null ){
            return ResponseEntity.ok(vendor);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<VendorResponse> registerVendor(@Valid @RequestBody VendorRequest request) {
        VendorResponse response = vendorService.registerVendor(request);
        if (response != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/users/{id}")
                    .buildAndExpand(response.getId())
                    .toUri();
            return ResponseEntity.created(location).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorResponse> updateVendor(@PathVariable Long id, @Valid @RequestBody VendorRequest request) {
        VendorResponse updatedVendor = vendorService.updateVendor(id, request);
        if (updatedVendor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedVendor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }
}
