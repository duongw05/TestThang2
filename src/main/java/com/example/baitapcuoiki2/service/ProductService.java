package com.example.baitapcuoiki2.service;

import com.example.baitapcuoiki2.dto.request.ProductRequest;
import com.example.baitapcuoiki2.dto.request.ProductSearchRequest;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.dto.response.ProductResponse;
import com.example.baitapcuoiki2.dto.response.ProductSearchResponse;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);


    PaginationDTO<ProductSearchResponse> searchProducts(ProductSearchRequest request, Pageable pageable);

    void exportProductToExcel(ProductSearchRequest dto, OutputStream outputStream) throws IOException;
}
