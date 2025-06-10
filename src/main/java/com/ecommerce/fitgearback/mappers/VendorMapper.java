package com.ecommerce.fitgearback.mappers;
import com.ecommerce.fitgearback.dtos.vendor.*;
import com.ecommerce.fitgearback.models.Role;
import com.ecommerce.fitgearback.models.Vendor;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface VendorMapper {
    Vendor toVendor(VendorRequest vendorRequest);
    VendorResponse toResponse(Vendor vendor);

    Vendor toUpdateVendor(UpdateVendorRequest vendorRequest);
    UpdateVendorResponse toUpdateResponse(Vendor vendor);
    default Set<String> mapRoles(@NotNull Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
