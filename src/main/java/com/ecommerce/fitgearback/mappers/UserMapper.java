package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.*;
import com.ecommerce.fitgearback.dtos.admin.AdminRequest;
import com.ecommerce.fitgearback.dtos.admin.AdminResponse;
import com.ecommerce.fitgearback.models.*;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.ComponentScan;

import java.util.Set;
import java.util.stream.Collectors;

@ComponentScan
@Mapper(componentModel = "spring")
public interface UserMapper {
    default Set<String> mapRoles(@NotNull Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
