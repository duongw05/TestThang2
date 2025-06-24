package com.example.baitapcuoiki2.service.impl;

import com.example.baitapcuoiki2.dto.request.CategoryRequest;
import com.example.baitapcuoiki2.dto.request.CategorySearchRequest;
import com.example.baitapcuoiki2.dto.response.CategoryExportResponse;
import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.dto.response.CategorySearchResponse;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.exception.CustomException.NotFoundException;
import com.example.baitapcuoiki2.exportExcel.CategoryExcelExporter;
import com.example.baitapcuoiki2.mapper.CategoryMapper;
import com.example.baitapcuoiki2.model.Category;
import com.example.baitapcuoiki2.model.CategoryImage;
import com.example.baitapcuoiki2.repository.CategoryRepository;
import com.example.baitapcuoiki2.repository.CategorySearchRepository;
import com.example.baitapcuoiki2.service.CategoryService;
import com.example.baitapcuoiki2.utils.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategorySearchRepository categorySearchRepository;

    private final MessageSource messageSource;

    @Transactional(rollbackFor = Throwable.class, isolation = Isolation.SERIALIZABLE)
    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = categoryMapper.toEntity(request);
        List<CategoryImage> imageList = new ArrayList<>();

        if (request.getCategoryImages() != null && !request.getCategoryImages().isEmpty()) {
            for (MultipartFile file : request.getCategoryImages()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        CategoryImage image = new CategoryImage();
                        image.setCategory(category);
                        image.setImage(file.getBytes());
                        image.setImageName(file.getOriginalFilename());
                        image.setStatus(Status.ACTIVE);
                        imageList.add(image);
                    } catch (IOException e) {
                        throw new RuntimeException(messageSource.getMessage(
                                "error.image.processing",
                                new Object[]{e.getMessage()},
                                LocaleContextHolder.getLocale()
                        ), e);
                    }
                }
            }
        }

        category.setStatus(Status.ACTIVE);
        category.setCategoryImages(imageList);
        Category saved = categoryRepository.save(category);

        return categoryMapper.toResponse(saved);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<CategoryResponse> list = categoryRepository.getAllCategoryDTO(Status.ACTIVE);
        return list;
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findCategoriesByStatusAndId(id,Status.ACTIVE).orElseThrow(()
                -> new NotFoundException( messageSource.getMessage("error.category.not.found", new Object[]{id}, LocaleContextHolder.getLocale())));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findCategoriesByStatus(id,Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException( messageSource.getMessage("error.category.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));

        category.setStatus(Status.INACTIVE);
        categoryRepository.save(category);
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findCategoriesByStatusAndId(id,Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException( messageSource.getMessage("error.category.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));

        categoryMapper.updateCategoryFromRequest(categoryRequest, category);
        category.setModifiedDate(new Date());
        category.setModifiedBy("admin");
        category.setStatus(Status.ACTIVE);

        if (categoryRequest.getOldImageIds() != null && !categoryRequest.getOldImageIds().isEmpty()) {
            for (Long oldImageId : categoryRequest.getOldImageIds()) {
                CategoryImage oldImage = category.getCategoryImages().stream()
                        .filter(img -> img.getId().equals(oldImageId) && img.getStatus() == Status.ACTIVE)
                        .findFirst()
                        .orElse(null);

                if (oldImage != null) {
                    oldImage.setStatus(Status.INACTIVE);
                }
            }
        }

        if (categoryRequest.getCategoryImages() != null && !categoryRequest.getCategoryImages().isEmpty()) {
            for (MultipartFile file : categoryRequest.getCategoryImages()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        CategoryImage newImage = new CategoryImage();
                        newImage.setCategory(category);
                        newImage.setImage(file.getBytes());
                        newImage.setStatus(Status.ACTIVE);
                        category.getCategoryImages().add(newImage);
                    } catch (IOException e) {
                        throw new RuntimeException(messageSource.getMessage(
                                "error.image.processing",
                                new Object[]{e.getMessage()},
                                LocaleContextHolder.getLocale()
                        ), e);
                    }
                }
            }
        }

        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public PaginationDTO<CategorySearchResponse> searchCategories(CategorySearchRequest request, Pageable pageable) {
        return categorySearchRepository.searchCategories(request, pageable);
    }

    @Override
    public void exportCategoriesToExcel(CategorySearchRequest dto, OutputStream outputStream) throws IOException {
        List<CategoryExportResponse> exportData = categorySearchRepository.timKiemXuatExcelDanhMuc(dto);

        if (exportData == null || exportData.isEmpty()) {
            String msg = messageSource.getMessage("error.noCategoryData.found", null, LocaleContextHolder.getLocale());
            throw new IOException(msg);
        }

        try (ByteArrayInputStream excelStream = CategoryExcelExporter.exportCategoryToExcel(exportData)) {
            outputStream.write(excelStream.readAllBytes());
        }
    }

}
