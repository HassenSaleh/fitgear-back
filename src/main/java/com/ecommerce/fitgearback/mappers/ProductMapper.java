package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.product.ProductRequest;
import com.ecommerce.fitgearback.dtos.product.ProductResponse;
import com.ecommerce.fitgearback.models.Category;
import com.ecommerce.fitgearback.models.Product;
import com.ecommerce.fitgearback.models.Vendor;
import org.mapstruct.*;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toProductResponse(Product product);

    List<ProductResponse> toProductResponseList(List<Product> products);

    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toProduct(ProductRequest productRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "category", ignore = true)
    void update(ProductRequest productRequest, @MappingTarget Product product);

}
