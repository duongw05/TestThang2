package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.model.CategoryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryImageRepository extends JpaRepository<CategoryImage,Long> {

}
