package com.ecommerce.fitgearback.controllers;

import com.ecommerce.fitgearback.dtos.login.JwtRequest;
import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.dtos.signup.SignUpResponse;
import com.ecommerce.fitgearback.dtos.vendor.*;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.services.AuthService;
import com.ecommerce.fitgearback.services.ProductService;
import com.ecommerce.fitgearback.services.VendorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@PreAuthorize(value = "hasRole('VENDOR')")
@Setter
@Getter
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/vendors")

public class VendorController {
    private final VendorService vendorService;
    private final ProductService productService;
    private final AuthService authService;

    @PreAuthorize(value = "hasRole('VENDOR')")
    @GetMapping("/")
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

    @PreAuthorize("permitAll()")
    @PostMapping("/sign")
    public ResponseEntity<SignUpResponse> sign(@Valid @RequestBody VendorRequest request, HttpServletResponse response) {
        VendorResponse vendorResponse = vendorService.sign(request);
        if (vendorResponse != null) {
            var jwtResponse = authService.login(new JwtRequest(vendorResponse.getEmail(), request.getPassword()), response);
            SignUpResponse signUpResponse = new SignUpResponse(jwtResponse.getAccessToken(), "/auth/login");

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(vendorResponse.getId())
                    .toUri();
            return ResponseEntity.created(location).body(signUpResponse);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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

    @PreAuthorize(value = "hasRole('VENDOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(value = "hasRole('VENDOR')")
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductResponse createdProduct = productService.createProduct(request);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }
    @PreAuthorize(value = "hasRole('VENDOR')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PreAuthorize(value = "hasRole('VENDOR')")
    @GetMapping("/{vendorId}/products")
    public ResponseEntity<Page<ProductResponse>> getProductsByVendor(@PathVariable Long vendorId, Pageable pageable) {
        try {
            if (vendorId == null || vendorId <= 0) {
                return ResponseEntity.badRequest().build();
            }

            Page<ProductResponse> productResponses = productService.getAllProductsByVendor(vendorId,pageable);


            if (productResponses.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(productResponses);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
