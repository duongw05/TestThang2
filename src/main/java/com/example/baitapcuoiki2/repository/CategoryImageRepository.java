package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.model.CategoryImage;
import com.example.baitapcuoiki2.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryImageRepository extends JpaRepository<CategoryImage,Long> {
    @Modifying
    @Query("UPDATE CategoryImage ci SET ci.status = :status WHERE ci.category.id = :categoryId AND ci.status = :currentStatus")
    int softDeleteCategoryImages(@Param("categoryId") Long categoryId,
                                 @Param("status") Status newStatus,
                                 @Param("currentStatus") Status currentStatus);

}
