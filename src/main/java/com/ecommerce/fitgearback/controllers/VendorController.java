package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.dtos.vendor.*;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.services.ProductService;
import com.ecommerce.fitgearback.services.VendorService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/vendor")
@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@PreAuthorize(value = "hasRole('VENDOR')")


public class VendorController {
    private final VendorService vendorService;
    private final ProductService productService;
    @PermitAll
    @GetMapping("/all")
    public ResponseEntity<Iterable<VendorResponse>> getAllVendors() {
        Iterable<VendorResponse> vendors = vendorService.getAllVendors();
        if(vendors.iterator().hasNext()){
            return ResponseEntity.ok(vendors);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize(value = "hasRole('VENDOR')")
    @GetMapping("/{vendorId}/dashboard")
    public ResponseEntity<VendorDashboardResponse> getDashboard(@PathVariable Long vendorId) {
        VendorDashboardResponse response = vendorService.getDashboard(vendorId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize(value = "hasRole('VENDOR')")
    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable Long id) {
        VendorResponse vendor = vendorService.getVendorById(id);
        if(vendor != null ){
            return ResponseEntity.ok(vendor);
        }
        return ResponseEntity.notFound().build();
    }

    @PermitAll
    @PostMapping("/sign")
    public ResponseEntity<VendorResponse> sign(@Valid @RequestBody VendorRequest request) {
        VendorResponse response = vendorService.sign(request);
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

    @PreAuthorize(value = "hasRole('VENDOR')")
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

    @PreAuthorize(value = "hasRole('VENDOR')")
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductResponse createdProduct = productService.createProduct(request, image);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }


    @PreAuthorize(value = "hasRole('VENDOR')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id, @RequestParam Long vendorId) {
        productService.deleteProduct(id, vendorId);
        return ResponseEntity.ok("Product deleted successfully");
    }


}
