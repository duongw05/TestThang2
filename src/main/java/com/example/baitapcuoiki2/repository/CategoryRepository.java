package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.model.Category;
import com.example.baitapcuoiki2.utils.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    @Query("""
    SELECT new com.example.baitapcuoiki2.dto.response.CategoryResponse(c.id, c.categoryName, c.categoryCode, c.description,c.status, c.createdDate,c.modifiedDate,c.createdBy,c.modifiedBy)
    FROM Category c
    WHERE c.status = :status
    ORDER BY c.id DESC
""")
    List<CategoryResponse> getAllCategoryDTO(@Param("status") Status status);

    @Query("""
    SELECT c FROM Category c
    LEFT JOIN FETCH c.categoryImages ci
    WHERE c.status = :status
      AND c.id = :categoryId
      AND (ci IS NULL OR ci.status = :status)
    """)
    Optional<Category> findCategoriesByStatusAndId(@Param("categoryId") Long categoryId, @Param("status") Status status);

    @Query("""
    SELECT c FROM Category c
    WHERE c.status = :status AND c.id = :categoryId
    """)
    Optional<Category> findCategoriesByStatus(@Param("categoryId") Long categoryId, @Param("status") Status status);

    List<Category> findAllByIdInAndStatus(List<Long> ids, Status status);

    boolean existsByCategoryCodeAndStatus(String categoryCode, Status status);

    @Query("SELECT c FROM Category c WHERE c.id IN :categoryIds")
    List<Category> findCategoriesByIds(@Param("categoryIds") List<Long> categoryIds);

    @Modifying
    @Query("UPDATE Category c SET c.status = :status WHERE c.id = :id AND c.status = :currentStatus")
    int softDeleteCategory(@Param("id") Long id,
                           @Param("status") Status newStatus,
                           @Param("currentStatus") Status currentStatus);


}
