package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.admin.*;
import com.ecommerce.fitgearback.models.Admin;
import com.ecommerce.fitgearback.models.Role;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    Admin toAdmin(AdminRequest adminRequest);
    AdminResponse toResponse(Admin admin);

    default Set<String> mapRoles(@NotNull Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
