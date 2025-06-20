package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.dto.request.ProductSearchRequest;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.dto.response.ProductExportResponse;
import com.example.baitapcuoiki2.dto.response.ProductResponse;
import com.example.baitapcuoiki2.dto.response.ProductSearchResponse;
import com.example.baitapcuoiki2.mapper.PaginationMapper;
import com.example.baitapcuoiki2.mapper.ProductExportMapper;
import com.example.baitapcuoiki2.mapper.ProductMapper;
import com.example.baitapcuoiki2.model.Category;
import com.example.baitapcuoiki2.model.Product;
import com.example.baitapcuoiki2.model.ProductCategory;
import com.example.baitapcuoiki2.model.ProductImage;
import com.example.baitapcuoiki2.utils.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ProductSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PaginationMapper paginationMapper;

    @Autowired
    private ProductExportMapper productExportMapper;

    public PaginationDTO<ProductSearchResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        String baseSql = "FROM Product p WHERE 1=1";
        StringBuilder whereClause = new StringBuilder(" AND p.status = :status");

        Map<String, Object> params = new HashMap<>();
        params.put("status", Status.ACTIVE);

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            whereClause.append(" AND (LOWER(p.productName) LIKE LOWER(:keyword) OR LOWER(p.productCode) LIKE LOWER(:keyword))");
            params.put("keyword", "%" + request.getKeyword().trim() + "%");
        }

        if (request.getCreatedFrom() != null) {
            whereClause.append(" AND p.createdDate >= :createdFrom");
            params.put("createdFrom", request.getCreatedFrom());
        }

        if (request.getCreatedTo() != null) {
            whereClause.append(" AND p.createdDate <= :createdTo");
            params.put("createdTo", request.getCreatedTo());
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            whereClause.append("""
            AND EXISTS (
                SELECT 1 FROM ProductCategory pc
                WHERE pc.product = p
                AND pc.category.id IN :categoryIds
                AND pc.status = :status
            )
        """);
            params.put("categoryIds", request.getCategoryIds());
        }

        String dataSql = "SELECT p " + baseSql + whereClause + " ORDER BY p.id DESC";
        TypedQuery<Product> query = entityManager.createQuery(dataSql, Product.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Product> products = query.getResultList();

        if (!products.isEmpty()) {
            Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());

            String productCategorySql = "SELECT pc FROM ProductCategory pc WHERE pc.product.id IN :productIds AND pc.status = :status";
            TypedQuery<ProductCategory> productCategoryQuery = entityManager.createQuery(productCategorySql, ProductCategory.class);
            productCategoryQuery.setParameter("productIds", productIds);
            productCategoryQuery.setParameter("status", Status.ACTIVE);
            System.out.println("status: "+Status.ACTIVE);
            List<ProductCategory> productCategories = productCategoryQuery.getResultList();

            Set<Long> categoryIds = productCategories.stream()
                    .map(pc -> pc.getCategory().getId())
                    .collect(Collectors.toSet());

            List<Category> categories = new ArrayList<>();
            if (!categoryIds.isEmpty()) {
                System.out.println("categoryId; "+categoryIds);
                String categorySql = "SELECT c FROM Category c WHERE c.id IN :categoryIds";
                TypedQuery<Category> categoryQuery = entityManager.createQuery(categorySql, Category.class);
                categoryQuery.setParameter("categoryIds", categoryIds);
                categories = categoryQuery.getResultList();
            }

            Map<Long, Category> categoryMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
            for (ProductCategory pc : productCategories) {
                pc.setCategory(categoryMap.get(pc.getCategory().getId()));
            }

            Map<Long, List<ProductCategory>> productCategoriesMap = productCategories.stream()
                    .collect(Collectors.groupingBy(pc -> pc.getProduct().getId()));

            for (Product product : products) {
                product.setProductCategories(productCategoriesMap.getOrDefault(product.getId(), new ArrayList<>()));
            }
        }

        String countSql = "SELECT COUNT(p) " + baseSql + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countSql, Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();

        List<ProductSearchResponse> responses = productMapper.toProductSearchResponses(products);
        Page<ProductSearchResponse> page = new PageImpl<>(responses, pageable, total);

        return paginationMapper.toPaginationDTO(page);
    }

    public List<ProductExportResponse> searchProductWithoutPaging(ProductSearchRequest dto) {
        Pageable pageable = Pageable.unpaged();

        PaginationDTO<ProductSearchResponse> resultWrapper = searchProducts(dto, pageable);

        List<ProductSearchResponse> searchResponses = resultWrapper.getData();

        List<ProductExportResponse> exportResponses = searchResponses.stream()
                .map(productExportMapper::toExportResponseFromSearchResponse)
                .collect(Collectors.toList());

        return exportResponses;
    }
}
