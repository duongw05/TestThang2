package com.example.baitapcuoiki2.service;

import com.example.baitapcuoiki2.dto.request.CategoryRequest;
import com.example.baitapcuoiki2.dto.request.CategorySearchRequest;
import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.dto.response.CategorySearchResponse;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    void deleteCategory(Long id);
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    PaginationDTO<CategorySearchResponse> searchCategories(CategorySearchRequest request, Pageable pageable);
    void exportCategoriesToExcel(CategorySearchRequest dto, OutputStream outputStream) throws IOException;
}
