package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.customer.CustomerRequest;
import com.ecommerce.fitgearback.dtos.customer.CustomerResponse;
import com.ecommerce.fitgearback.models.Customer;
import com.ecommerce.fitgearback.models.Role;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toCustomer(CustomerRequest customerRequest);
    CustomerResponse toResponse(Customer customer);
    List<CustomerResponse> toCustomerResponseList(List<Customer> customers);

    default Set<String> mapRoles(@NotNull Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
