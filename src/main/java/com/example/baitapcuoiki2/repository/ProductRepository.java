package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.model.Product;
import com.example.baitapcuoiki2.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    @Query("""
        select p from Product p where p.id =:id and p.status = :status
""")
    Optional<Product> getOneByStatus(@Param("id") Long productId, @Param("status") Status status);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.status = :status")
    Optional<Product> findByIdAndStatus(@Param("id") Long id, @Param("status") Status status);


    @Query("SELECT COUNT(p.id) > 0 FROM Product p WHERE p.productCode = :productCode AND p.status = :status")
    boolean existsActiveProductCode(@Param("productCode") String productCode,@Param("status") Status status);

}
