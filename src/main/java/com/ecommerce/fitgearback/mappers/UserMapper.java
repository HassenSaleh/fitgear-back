package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.*;
import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.models.Vendor;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.ComponentScan;

import java.util.Set;
import java.util.stream.Collectors;
@ComponentScan
@Mapper(componentModel = "spring")
public interface UserMapper {
    Admin toAdmin(AdminDto dto);
    AdminResponseDto toAdminResponseDTO(Admin admin);
    Vendor toVendor(VendorDto dto);
    VendorResponseDto toVendorResponseDTO(Vendor vendor);
    Customer toCustomer(CustomerDto dto);
    CustomerResponseDto toCustomerResponseDTO(Customer customer);

    default Set<String> mapRoles(@NotNull Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

}
