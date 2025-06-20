package com.example.baitapcuoiki2.mapper;

import com.example.baitapcuoiki2.dto.response.ProductCategoryResponse;
import com.example.baitapcuoiki2.model.ProductCategory;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

public interface ProductCategoryMapper {

    ProductCategoryResponse toResponse(ProductCategory productCategory);

    List<ProductCategoryResponse> toResponseList(List<ProductCategory> productCategories);

    @Mapping(target = "product", ignore = true) // Không ánh xạ Product
    @Mapping(target = "category", ignore = true) // Không ánh xạ Category
    void updateProductCategoryFromRequest(ProductCategory productCategory, @MappingTarget ProductCategory entity);
}
