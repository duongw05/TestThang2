package com.example.baitapcuoiki2.mapper;

import com.example.baitapcuoiki2.dto.request.CategoryRequest;
import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.dto.response.CategorySearchResponse;
import com.example.baitapcuoiki2.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryImageMapper.class})
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);


    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "categoryImages", ignore = true)
    void updateCategoryFromRequest(CategoryRequest dto, @MappingTarget Category entity);

    List<CategorySearchResponse> toCategorySearchsResponse(List<Category> categorys);
}