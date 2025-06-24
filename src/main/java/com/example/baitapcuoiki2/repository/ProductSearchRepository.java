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
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private PaginationMapper paginationMapper;

    @Autowired
    private ProductExportMapper productExportMapper;

    public PaginationDTO<ProductSearchResponse> searchProducts(ProductSearchRequest request, Pageable pageable) {
        String sql = """
                 SELECT p.id, p.product_name, p.product_code, p.description, p.price,
                 p.quantity, p.createdDate, p.modifiedDate, p.createdBy, p.modifiedBy,
                 GROUP_CONCAT(c.category_name SEPARATOR ',') AS category_names
                 FROM Product p
                 LEFT JOIN ProductCategory pc ON p.id = pc.productId
                 LEFT JOIN Category c ON c.id = pc.categoryId AND p.status = 1 and pc.status = 1
                """;
        StringBuilder whereClause = new StringBuilder(" WHERE p.status = :status ");
        Map<String, Object> params = new HashMap<>();
        params.put("status", Status.ACTIVE);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            whereClause.append(" AND (LOWER(p.product_name) LIKE LOWER(:keyword) OR LOWER(p.product_code) LIKE LOWER(:keyword)) ");
            params.put("keyword", "%" + request.getKeyword().trim() + "%");
        }

        if (request.getCreatedFrom() != null) {
            whereClause.append(" AND p.createdDate >= :createdFrom ");
            params.put("createdFrom", request.getCreatedFrom());
        }

        if (request.getCreatedTo() != null) {
            whereClause.append(" AND p.createdDate <= :createdTo ");
            params.put("createdTo", request.getCreatedTo());
        }

        if (request.getCategoryIds() != null ) {
            whereClause.append(" AND c.id = :categoryId ");
            params.put("categoryId", request.getCategoryIds());
        }

        StringBuilder dataSql = new StringBuilder();
        dataSql.append(sql).append(whereClause).append("""
            GROUP BY p.id, p.product_name, p.product_code, p.description, p.price,
                     p.quantity, p.createdDate, p.modifiedDate, p.createdBy, p.modifiedBy
            ORDER BY p.id DESC
            """);

        if (!pageable.isUnpaged()) {
            dataSql.append(" LIMIT :limit OFFSET :offset ");
        }

        Query query = entityManager.createNativeQuery(dataSql.toString());
        params.forEach(query::setParameter);
        if (!pageable.isUnpaged()) {
            query.setParameter("limit", pageable.getPageSize());
            query.setParameter("offset", pageable.getOffset());
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<ProductSearchResponse> results = rows.stream().map(row -> {
            ProductSearchResponse dto = new ProductSearchResponse();
            dto.setId(((Number) row[0]).longValue());
            dto.setProductName((String) row[1]);
            dto.setProductCode((String) row[2]);
            dto.setDescription((String) row[3]);
            dto.setPrice(row[4] != null ? ((Number) row[4]).doubleValue() : null);
            dto.setQuantity(row[5] != null ? ((Number) row[5]).longValue() : null);
            dto.setCreatedDate((Date) row[6]);
            dto.setModifiedDate((Date) row[7]);
            dto.setCreatedBy((String) row[8]);
            dto.setModifiedBy((String) row[9]);
            dto.setCategoryNames(row[10] != null ? List.of(((String) row[10]).split(",")) : List.of());
            return dto;
        }).toList();

        String countSql = """
        SELECT COUNT(DISTINCT p.id)
        FROM Product p
        LEFT JOIN ProductCategory pc ON p.id = pc.productId
        LEFT JOIN Category c ON c.id = pc.categoryId AND p.status = 1 and pc.status = 1
    """ + whereClause;

        Query countQuery = entityManager.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        Page<ProductSearchResponse> page = new PageImpl<>(results, pageable, total);
        return paginationMapper.toPaginationDTO(page);
    }

    public List<ProductExportResponse> timKiemXuatExcelSanPham(ProductSearchRequest dto) {
        Pageable pageable = Pageable.unpaged();

        PaginationDTO<ProductSearchResponse> resultWrapper = searchProducts(dto, pageable);

        List<ProductSearchResponse> searchResponses = resultWrapper.getData();

        List<ProductExportResponse> exportResponses = searchResponses.stream()
                .map(productExportMapper::toExportResponseFromSearchResponse)
                .collect(Collectors.toList());

        return exportResponses;
    }

}
