package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.VendorDto;
import com.ecommerce.fitgearback.dtos.VendorResponseDto;
import com.ecommerce.fitgearback.models.Vendor;
import org.mapstruct.Mapper;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@Mapper(componentModel = "spring")
public interface VendorMapper {

}
