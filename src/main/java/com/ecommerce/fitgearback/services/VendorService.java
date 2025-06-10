package com.ecommerce.fitgearback.services;

import com.ecommerce.fitgearback.dtos.vendor.*;
import com.ecommerce.fitgearback.exceptions.ResourceNotFoundException;
import com.ecommerce.fitgearback.mappers.OrderMapper;
import com.ecommerce.fitgearback.mappers.VendorMapper;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.models.Vendor;
import com.ecommerce.fitgearback.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@AllArgsConstructor
@Service
public class VendorService {
    private final VendorRepository vendorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VendorMapper vendorMapper;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final AuthService authService;

    @Transactional
    public VendorResponse sign(VendorRequest vendorRequest) {
        Vendor vendor = vendorMapper.toVendor(vendorRequest);
        vendor.setPassword(passwordEncoder.encode(vendorRequest.getPassword()));
        Role vendorRole = roleRepository
                .findByName("VENDOR")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        vendor.setRoles(Collections.singleton(vendorRole));
        Vendor savedVendor = vendorRepository.save(vendor);

        return vendorMapper.toResponse(savedVendor);
    }
    public List<VendorResponse> getAllVendors() {
        return vendorRepository.findAll()
                .stream()
                .map(vendorMapper::toResponse)
                .collect(Collectors.toList());
    }

    public VendorResponse getVendorById(Long id) {
        Vendor vendor = vendorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
        return vendorMapper.toResponse(vendor);
    }


    public UpdateVendorResponse updateVendor(Long id, @NotNull UpdateVendorRequest request) {
        Vendor existing = vendorRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(existing.getEmail());
        existing.setPassword(existing.getPassword());
        existing.setContactNumber(request.getContactNumber());
        System.out.println(request.getContactNumber());
        existing.setCompanyAddress(request.getCompanyAddress());
        existing.setCompanyName(request.getCompanyName());
        Vendor updated = vendorRepository.save(existing);
        return vendorMapper.toUpdateResponse(updated);
    }

    public void deleteVendor(Long id) {
        if (!vendorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendor not found with id: " + id);
        }
        vendorRepository.deleteById(id);
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var vendor = vendorRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));
        return new User(
                vendor.getEmail(),
                vendor.getPassword(),
                Collections.emptyList()
        );
    }

    public VendorDashboardResponse getDashboard(Long vendorId) {
        Vendor currentVendor = authService.getCurrentVendor();
        if (!currentVendor.getId().equals(vendorId)) {
            throw new AccessDeniedException("You can only view your own orders.");
        }
        long totalOrders = orderRepository.countOrdersByVendorId(vendorId);
        long totalCustomers = orderRepository.countDistinctCustomersByVendorId(vendorId);
        long totalProducts = productRepository.countByVendorId(vendorId);
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenueByVendorId(vendorId);

        return new VendorDashboardResponse(totalOrders, totalCustomers, totalProducts, totalRevenue);
    }
}
