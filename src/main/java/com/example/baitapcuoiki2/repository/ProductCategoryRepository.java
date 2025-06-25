package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.dto.response.CategorySearchResponse;
import com.example.baitapcuoiki2.model.Category;
import com.example.baitapcuoiki2.model.ProductCategory;
import com.example.baitapcuoiki2.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Long> {

    @Query("SELECT pc FROM ProductCategory pc JOIN FETCH pc.category WHERE pc.product.id = :productId AND pc.status = :status")
    List<ProductCategory> getAllByProductAndStatus(@Param("productId") Long productId, @Param("status") Status status);

    @Query("""
    SELECT new com.example.baitapcuoiki2.dto.response.CategorySearchResponse(
        c.id,c.categoryCode,c.categoryName,c.description,c.status,
        c.createdDate,c.modifiedDate,c.createdBy,c.modifiedBy
    )
    FROM ProductCategory pc
    JOIN pc.category c
    WHERE pc.product.id = :productId AND pc.status = :status
""")
    List<CategorySearchResponse> findActiveCategoryResponsesByProductId(@Param("productId") Long productId,@Param("status") Status status);


    @Query("SELECT pc FROM ProductCategory pc WHERE pc.product.id = :productId AND pc.category.id IN :categoryIds AND pc.status = :status")
    List<ProductCategory> findAllSoftDeletedCategories(@Param("productId") Long productId,
                                                       @Param("categoryIds") List<Long> categoryIds,
                                                       @Param("status") Status status);

    @Modifying
    @Query("UPDATE ProductCategory pc SET pc.status = :status WHERE pc.product.id = :productId AND pc.status = :currentStatus")
    int softDeleteProductCategoriesByProductId(@Param("productId") Long productId,
                                               @Param("status") Status status,
                                               @Param("currentStatus") Status currentStatus);



}
