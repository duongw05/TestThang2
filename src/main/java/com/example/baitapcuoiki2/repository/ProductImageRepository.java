package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.model.ProductImage;
import com.example.baitapcuoiki2.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.status = :status WHERE pi.product.id = :productId AND pi.status = :currentStatus")
    int softDeleteImagesByProductId(@Param("productId") Long productId,
                                    @Param("status") Status status,
                                    @Param("currentStatus") Status currentStatus);

}
