package com.ecommerce.fitgearback.mappers;

import com.ecommerce.fitgearback.dtos.category.CategoryRequest;
import com.ecommerce.fitgearback.dtos.category.CategoryResponse;
import com.ecommerce.fitgearback.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);
    Category toCategory(CategoryRequest categoryRequest);
    CategoryResponse toCategoryResponse(Category category);
}
