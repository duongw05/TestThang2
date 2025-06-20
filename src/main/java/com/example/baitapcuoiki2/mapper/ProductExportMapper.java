package com.example.baitapcuoiki2.mapper;

import com.example.baitapcuoiki2.dto.response.ProductExportResponse;
import com.example.baitapcuoiki2.dto.response.ProductSearchResponse;
import com.example.baitapcuoiki2.model.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductExportMapper {
    ProductExportResponse toExportResponseFromSearchResponse(ProductSearchResponse dto);
}
