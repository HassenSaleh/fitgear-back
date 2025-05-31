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

    @Mapping(target = "imageUrl", expression = "java(product != null && product.getId() != null ? \"/product/\" + product.getId() + \"/image\" : null)")
    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "imageBase64", expression = "java(product.getImageData() != null ? java.util.Base64.getEncoder().encodeToString(product.getImageData()) : null)")
    ProductResponse toProductResponse(Product product);
    List<ProductResponse> toProductResponseList(List<Product> product);
    @Mapping(target = "vendor", expression = "java(mapVendor(productRequest.getVendorId()))")
    @Mapping(target = "category", expression = "java(mapCategory(productRequest.getCategoryId()))")
    Product toProduct(ProductRequest productRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "vendor", expression = "java(mapVendor(productRequest.getVendorId()))")
    @Mapping(target = "category", expression = "java(mapCategory(productRequest.getCategoryId()))")
    void update(ProductRequest productRequest, @MappingTarget Product product);

    default String toBase64(byte[] imageData) {
        return imageData != null ? Base64.getEncoder().encodeToString(imageData) : null;
    }

    default Vendor mapVendor(Long vendorId) {
        if (vendorId == null) return null;
        Vendor vendor = new Vendor();
        vendor.setId(vendorId);
        return vendor;
    }

    default Category mapCategory(Long categoryId) {
        if (categoryId == null) return null;
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }


}
