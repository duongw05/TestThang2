package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.model.ProductImage;
import com.example.baitapcuoiki2.utils.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {

}
