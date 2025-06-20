package com.example.baitapcuoiki2.service.impl;

import com.example.baitapcuoiki2.dto.request.ProductRequest;
import com.example.baitapcuoiki2.dto.request.ProductSearchRequest;
import com.example.baitapcuoiki2.dto.response.CategoryResponse;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.dto.response.ProductExportResponse;
import com.example.baitapcuoiki2.dto.response.ProductResponse;
import com.example.baitapcuoiki2.dto.response.ProductSearchResponse;
import com.example.baitapcuoiki2.exportExcel.ProductExcelExporter;
import com.example.baitapcuoiki2.mapper.CategoryMapper;
import com.example.baitapcuoiki2.mapper.ProductMapper;
import com.example.baitapcuoiki2.model.Category;
import com.example.baitapcuoiki2.model.Product;
import com.example.baitapcuoiki2.model.ProductCategory;
import com.example.baitapcuoiki2.model.ProductCategoryId;
import com.example.baitapcuoiki2.model.ProductImage;
import com.example.baitapcuoiki2.repository.CategoryRepository;
import com.example.baitapcuoiki2.repository.ProductCategoryRepository;
import com.example.baitapcuoiki2.repository.ProductImageRepository;
import com.example.baitapcuoiki2.repository.ProductRepository;
import com.example.baitapcuoiki2.repository.ProductSearchRepository;
import com.example.baitapcuoiki2.service.ProductService;
import com.example.baitapcuoiki2.utils.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final MessageSource messageSource;


    @Transactional(rollbackFor = Throwable.class)
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        product.setStatus(Status.ACTIVE);
        product.setCreatedDate(new Date());
        product.setModifiedBy("admin");
        product.setCreatedBy("admin");

        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            List<ProductImage> imageList = request.getProductImages().stream().map(file -> {
                try {
                    ProductImage image = new ProductImage();
                    image.setImage(file.getBytes());
                    image.setImageName(file.getOriginalFilename());
                    image.setStatus(Status.ACTIVE);
                    image.setCreatedDate(new Date());
                    image.setCreatedBy("admin");
                    image.setModifiedBy("admin");
                    image.setProduct(product); // để cascade hoạt động
                    return image;
                } catch (IOException e) {
                    throw new RuntimeException(messageSource.getMessage(
                            "error.image.processing",
                            new Object[]{e.getMessage()},
                            LocaleContextHolder.getLocale()
                    ), e);
                }
            }).collect(Collectors.toList());

            product.setProductImages(imageList);
        }

        Product savedProduct = productRepository.save(product);

        List<Category> categories = categoryRepository.findAllByIdInAndStatus(
                request.getCategories(), Status.ACTIVE
        );

        if (categories.size() != request.getCategories().size()) {
            throw new RuntimeException(messageSource.getMessage(
                    "error.category.not-found",
                    null,
                    LocaleContextHolder.getLocale()
            ));
        }

        List<ProductCategory> productCategories = categories.stream().map(category -> {
            ProductCategory pc = new ProductCategory();
            ProductCategoryId pcId = new ProductCategoryId(savedProduct.getId(), category.getId());
            pc.setId(pcId);
            pc.setProduct(savedProduct);
            pc.setCategory(category);
            pc.setStatus(Status.ACTIVE);
            pc.setCreatedBy("admin");
            pc.setCreatedDate(new Date());
            pc.setModifiedDate(new Date());
            return pc;
        }).collect(Collectors.toList());

        productCategoryRepository.saveAll(productCategories);

        ProductResponse response = productMapper.toResponse(savedProduct);
        response.setCategories(categories.stream().map(categoryMapper::toResponse).collect(Collectors.toList()));

        return response;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.getOneByStatus(id,Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("product.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));

        productMapper.updateProductFromRequest(request, existingProduct);
        existingProduct.setModifiedDate(new Date());
        existingProduct.setModifiedBy("admin");

        if (request.getOldImageIds() != null && !request.getOldImageIds().isEmpty()) {
            List<ProductImage> allImages = productImageRepository.findAllById(request.getOldImageIds());

            List<Long> invalidImageIds = request.getOldImageIds().stream()
                    .filter(idImage -> allImages.stream()
                            .noneMatch(img -> img.getId().equals(idImage) && !"0".equals(img.getStatus())))
                    .collect(Collectors.toList());

            if (!invalidImageIds.isEmpty()) {
                throw new RuntimeException(messageSource.getMessage(
                        "error.image.not-found",
                        new Object[]{invalidImageIds},
                        LocaleContextHolder.getLocale()
                ));
            }

            for (ProductImage image : allImages) {
                image.setStatus(Status.INACTIVE);
                image.setModifiedDate(new Date());
                image.setModifiedBy("admin");
            }
        }

        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            for (MultipartFile file : request.getProductImages()) {
                try {
                    ProductImage newImage = new ProductImage();
                    newImage.setImage(file.getBytes());
                    newImage.setImageName(file.getOriginalFilename());
                    newImage.setStatus(Status.ACTIVE);
                    newImage.setCreatedDate(new Date());
                    newImage.setCreatedBy("admin");
                    newImage.setModifiedBy("admin");
                    newImage.setProduct(existingProduct);

                    existingProduct.getProductImages().add(newImage);
                } catch (IOException e) {
                    throw new RuntimeException(
                            messageSource.getMessage("error.image.processing", null, LocaleContextHolder.getLocale()), e);
                }
            }
        }

        List<ProductCategory> existingCategories = productCategoryRepository.getAllByProductAndStatus(id,Status.ACTIVE);

        List<Long> categoryIds = existingCategories.stream()
                .map(pc -> pc.getCategory().getId())
                .distinct()
                .toList();

        List<Category> categories = categoryIds.isEmpty() ?
                Collections.emptyList() :
                categoryRepository.findCategoriesByIds(categoryIds);

        if (categories.isEmpty()) {
            throw new RuntimeException(messageSource.getMessage(
                    "error.category.not-found",
                    null,
                    LocaleContextHolder.getLocale()
            ));
        }

        Map<Long, Category> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        for (ProductCategory pc : existingCategories) {
            pc.setCategory(categoryMap.get(pc.getCategory().getId()));
        }

        Set<Category> finalCategories = new HashSet<>();

        if (request.getCategories() == null || request.getCategories().isEmpty()) {
            for (ProductCategory pc : existingCategories) {
                finalCategories.add(pc.getCategory());
            }
        } else {
            Set<Long> newCategoryIds = new HashSet<>(request.getCategories());

            for (ProductCategory pc : existingCategories) {
                Long categoryId = pc.getCategory().getId();
                if (!newCategoryIds.contains(categoryId)) {
                    pc.setStatus(Status.ACTIVE);
                    pc.setModifiedDate(new Date());
                    pc.setModifiedBy("admin");
                } else {
                    finalCategories.add(pc.getCategory());
                }
            }

            List<Category> newCategories = categoryRepository.findAllByIdInAndStatus(new ArrayList<>(newCategoryIds), Status.ACTIVE);

            for (Category newCat : newCategories) {
                boolean exists = existingCategories.stream()
                        .anyMatch(pc -> pc.getCategory().getId().equals(newCat.getId()));

                if (!exists) {
                    Optional<ProductCategory> softDeleted = productCategoryRepository
                            .findByProductIdAndCategoryIdAndStatus(id, newCat.getId(), Status.INACTIVE);

                    if (softDeleted.isPresent()) {
                        ProductCategory pc = softDeleted.get();
                        pc.setStatus(Status.ACTIVE);
                        pc.setModifiedDate(new Date());
                        pc.setModifiedBy("admin");
                    } else {
                        ProductCategory pc = new ProductCategory();
                        pc.setId(new ProductCategoryId(existingProduct.getId(), newCat.getId()));
                        pc.setProduct(existingProduct);
                        pc.setCategory(newCat);
                        pc.setStatus(Status.ACTIVE);
                        pc.setCreatedDate(new Date());
                        pc.setModifiedDate(new Date());
                        pc.setCreatedBy("admin");
                        pc.setModifiedBy("admin");

                        productCategoryRepository.save(pc);
                    }

                    finalCategories.add(newCat);
                }
            }
        }

        productRepository.save(existingProduct);

        ProductResponse response = productMapper.toResponse(existingProduct);
        response.setCategories(finalCategories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList()));

        return response;
    }
    @Transactional
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.getOneByStatus(id,Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage("product.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));

        product.setStatus(Status.ACTIVE);
        product.setModifiedDate(new Date());
        product.setModifiedBy("admin");

        List<ProductImage> productImages = product.getProductImages();
        if (productImages != null && !productImages.isEmpty()) {
            productImages.forEach(image -> {
                image.setStatus(Status.INACTIVE);
                image.setModifiedDate(new Date());
                image.setModifiedBy("admin");
            });
        }

        List<ProductCategory> productCategories = productCategoryRepository.getAllByProductAndStatus(id,Status.ACTIVE);
        if (!productCategories.isEmpty()) {
            productCategories.forEach(pc -> {
                pc.setStatus(Status.INACTIVE);
                pc.setModifiedDate(new Date());
                pc.setModifiedBy("admin");
            });
        }

        productRepository.save(product);
        if (productImages != null && !productImages.isEmpty()) {
            productImageRepository.saveAll(productImages);
        }
        if (!productCategories.isEmpty()) {
            productCategoryRepository.saveAll(productCategories);
        }
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findActiveProductWithImages(id,Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException(messageSource.getMessage("product.not.found", new Object[]{id}, LocaleContextHolder.getLocale())
                ));

        List<CategoryResponse> categoryResponses = productCategoryRepository.findActiveCategoryResponsesByProductId(id,Status.ACTIVE);

        ProductResponse response = productMapper.toResponse(product);
        response.setCategories(categoryResponses);

        return response;
    }

    @Override
    public PaginationDTO<ProductSearchResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        return productSearchRepository.searchProducts(request, pageable);
    }

    @Override
    public void exportProductToExcel(ProductSearchRequest dto, OutputStream outputStream) throws IOException {
        // Kiểm tra đầu vào
        if (dto == null) {
            String errorMessage = messageSource.getMessage("error.productSearchRequest.null", null, LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(errorMessage);
        }
        if (outputStream == null) {
            String errorMessage = messageSource.getMessage("error.outputStream.null", null, LocaleContextHolder.getLocale());
            throw new IllegalArgumentException(errorMessage);
        }

        List<ProductExportResponse> exportData = null;
        try {
            exportData = productSearchRepository.searchProductWithoutPaging(dto);
        } catch (Exception e) {
            String errorMessage = messageSource.getMessage("error.productData.fetch", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale());
            throw new IOException(errorMessage, e);
        }

        if (exportData == null || exportData.isEmpty()) {
            String errorMessage = messageSource.getMessage("error.noProductData.found", null, LocaleContextHolder.getLocale());
            throw new IOException(errorMessage);
        }

        System.out.println("list sau tìm kiếm: " + exportData.size());

        ByteArrayInputStream excelStream = null;
        try {
            excelStream = ProductExcelExporter.exportProductToExcel(exportData);
            if (excelStream == null) {
                String errorMessage = messageSource.getMessage("error.excelCreation.failed", null, LocaleContextHolder.getLocale());
                throw new IOException(errorMessage);
            }
            outputStream.write(excelStream.readAllBytes());
        } catch (IOException e) {
            String errorMessage = messageSource.getMessage("error.productData.export", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale());
            throw new IOException(errorMessage, e);
        } finally {
            if (excelStream != null) {
                try {
                    excelStream.close();
                } catch (IOException e) {
                    String errorMessage = messageSource.getMessage("error.excelStream.close.failed", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale());
                    System.err.println(errorMessage);
                }
            }
        }
    }
}
