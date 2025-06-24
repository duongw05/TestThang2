package com.example.baitapcuoiki2.controller;

import com.example.baitapcuoiki2.dto.request.ProductRequest;
import com.example.baitapcuoiki2.dto.request.ProductSearchRequest;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.dto.response.ProductResponse;
import com.example.baitapcuoiki2.dto.response.ProductSearchResponse;
import com.example.baitapcuoiki2.exception.ErrorResponse;
import com.example.baitapcuoiki2.service.ProductService;
import com.example.baitapcuoiki2.validations.Validations;
import com.example.baitapcuoiki2.validations.ValidatorUtil;;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final MessageSource messageSource;
    private final Validator validator;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct( @Validated(Validations.create.class) @ModelAttribute ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Validated(Validations.update.class)
                                                         @ModelAttribute ProductRequest request
                                                         ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ErrorResponse> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        String successMessage = messageSource.getMessage(
                "product.delete.success",
                null,
                LocaleContextHolder.getLocale()
        );
        ErrorResponse response = new com.example.baitapcuoiki2.exception.ErrorResponse(
                200,
                successMessage,
                new Date(),
                null
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/search")
    public ResponseEntity<PaginationDTO<ProductSearchResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date createdFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date createdTo,
            @RequestParam(required = false) Long categoryIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ProductSearchRequest request = new ProductSearchRequest();
        request.setKeyword(keyword);
        request.setCreatedFrom(createdFrom);
        request.setCreatedTo(createdTo);
        request.setCategoryIds(categoryIds);
        request.setPage(page);
        request.setSize(size);

        ValidatorUtil.validateRequest(request, validator, messageSource);

        Pageable pageable = PageRequest.of(page,size);

        return ResponseEntity.ok(productService.searchProducts(request, pageable));
    }


    @PostMapping("/export")
    public void exportExcel(@RequestBody ProductSearchRequest dto, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=danhsachsanpham.xlsx");

        productService.exportProductToExcel(dto, response.getOutputStream());
        response.flushBuffer();
    }

}
