package com.example.baitapcuoiki2.mapper;

import com.example.baitapcuoiki2.dto.request.ProductRequest;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.dto.response.ProductResponse;
import com.example.baitapcuoiki2.dto.response.ProductSearchResponse;
import com.example.baitapcuoiki2.model.Product;
import com.example.baitapcuoiki2.model.ProductCategory;
import com.example.baitapcuoiki2.utils.Status;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",  uses = {ProductImageMapper.class, CategoryMapper.class})
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product product);

    @Mapping(source = "productCategories", target = "categoryNames", qualifiedByName = "mapCategoryNames")
    ProductSearchResponse toProductSearchResponse(Product product);

    List<ProductSearchResponse> toProductSearchResponses(List<Product> products);

    @Named("mapCategoryNames")
    default List<String> mapCategoryNames(List<ProductCategory> productCategories) {
        if (productCategories == null) return List.of();

        return productCategories.stream()
                .filter(pc -> pc.getStatus() == Status.ACTIVE)
                .map(pc -> pc.getCategory().getCategoryName())
                .collect(Collectors.toList());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateProductFromRequest(ProductRequest dto, @MappingTarget Product entity);
}
