package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.model.Category;
import com.example.baitapcuoiki2.model.ProductCategory;
import com.example.baitapcuoiki2.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Long> {

    @Query("""
    select pc from ProductCategory pc where pc.product.id =:productId and pc.status = :status
""")
    List<ProductCategory> getAllByProductAndStatus(@Param("productId") Long productId,@Param("status") Status status);

    @Query("""
    SELECT new com.example.baitapcuoiki2.dto.response.CategoryResponse(
        c.id, c.categoryName, c.categoryCode, c.description, c.status,
        c.createdDate, c.modifiedDate, c.createdBy, c.modifiedBy
    )
    FROM ProductCategory pc
    JOIN pc.category c
    WHERE pc.product.id = :productId AND pc.status = :status
""")
    List<CategoryResponse> findActiveCategoryResponsesByProductId(@Param("productId") Long productId,
                                                                  @Param("status") Status status);

    Optional<ProductCategory> findByProductIdAndCategoryIdAndStatus(Long productId, Long categoryId, Status status);

}
