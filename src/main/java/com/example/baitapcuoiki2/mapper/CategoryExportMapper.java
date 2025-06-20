package com.example.baitapcuoiki2.mapper;

import com.example.baitapcuoiki2.dto.response.CategoryExportResponse;
import com.example.baitapcuoiki2.dto.response.CategorySearchResponse;
import com.example.baitapcuoiki2.model.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryExportMapper {
    CategoryExportResponse toExportResponseFromSearchResponse(CategorySearchResponse dto);

}
