package com.example.baitapcuoiki2.controller;

import com.example.baitapcuoiki2.dto.request.CategoryRequest;
import com.example.baitapcuoiki2.dto.request.CategorySearchRequest;
import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.dto.response.CategorySearchResponse;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.exception.ErrorResponse;
import com.example.baitapcuoiki2.service.CategoryService;
import com.example.baitapcuoiki2.validations.Validations;
import com.example.baitapcuoiki2.validations.ValidatorUtil;
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
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final MessageSource messageSource;
    private final Validator validator;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
            @Validated(Validations.create.class) @ModelAttribute CategoryRequest categoryRequest) {
        return categoryService.createCategory(categoryRequest);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Validated(Validations.update.class) @ModelAttribute CategoryRequest categoryRequest) {
        return categoryService.updateCategory(id, categoryRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ErrorResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        String successMessage = messageSource.getMessage("category.delete.success", null, LocaleContextHolder.getLocale());
        ErrorResponse response = new ErrorResponse(200, successMessage, new Date(), null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginationDTO<CategorySearchResponse>> searchCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date createdFrom,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss") Date createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        CategorySearchRequest request = new CategorySearchRequest();
        request.setKeyword(keyword);
        request.setCreatedFrom(createdFrom);
        request.setCreatedTo(createdTo);
        request.setPage(page);
        request.setSize(size);

        ValidatorUtil.validateRequest(request, validator, messageSource);

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(categoryService.searchCategories(request, pageable));
    }

    @PostMapping("/export")
    public void exportExcel(@RequestBody CategorySearchRequest dto, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=categories.xlsx");

        categoryService.exportCategoriesToExcel(dto, response.getOutputStream());
        response.flushBuffer();
    }

}
