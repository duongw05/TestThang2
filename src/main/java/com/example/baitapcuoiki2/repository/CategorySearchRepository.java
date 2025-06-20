package com.example.baitapcuoiki2.repository;

import com.example.baitapcuoiki2.dto.request.CategorySearchRequest;
import com.example.baitapcuoiki2.dto.response.CategoryExportResponse;
import com.example.baitapcuoiki2.dto.response.CategorySearchResponse;
import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import com.example.baitapcuoiki2.mapper.CategoryExportMapper;
import com.example.baitapcuoiki2.mapper.CategoryMapper;
import com.example.baitapcuoiki2.mapper.PaginationMapper;
import com.example.baitapcuoiki2.model.Category;
import com.example.baitapcuoiki2.model.CategoryImage;
import com.example.baitapcuoiki2.utils.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CategorySearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryExportMapper categoryExportMapper;

    @Autowired
    private PaginationMapper paginationMapper;

    public PaginationDTO<CategorySearchResponse> searchCategories(CategorySearchRequest dto, Pageable pageable) {
        String baseSql = "FROM Category c WHERE 1=1";
        StringBuilder whereClause = new StringBuilder(" AND c.status = :status");

        Map<String, Object> params = new HashMap<>();
        params.put("status", Status.ACTIVE);

        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            whereClause.append(" AND (LOWER(c.categoryName) LIKE LOWER(:keyword) OR LOWER(c.categoryCode) LIKE LOWER(:keyword))");
            params.put("keyword", "%" + dto.getKeyword().trim() + "%");
        }

        if (dto.getCreatedFrom() != null) {
            whereClause.append(" AND c.createdDate >= :createdFrom");
            params.put("createdFrom", dto.getCreatedFrom());
        }

        if (dto.getCreatedTo() != null) {
            whereClause.append(" AND c.createdDate <= :createdTo");
            params.put("createdTo", dto.getCreatedTo());
        }

        String dataSql = "SELECT c " + baseSql + whereClause + " ORDER BY c.id DESC";
        TypedQuery<Category> query = entityManager.createQuery(dataSql, Category.class);
        params.forEach(query::setParameter);
        if (!pageable.isUnpaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        List<Category> categories = query.getResultList();

        String countSql = "SELECT COUNT(c) " + baseSql + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countSql, Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();

        List<CategorySearchResponse> dtoList = categoryMapper.toCategorySearchsResponse(categories);
        Page<CategorySearchResponse> page = new PageImpl<>(dtoList, pageable, total);

        return paginationMapper.toPaginationDTO(page);
    }

    public List<CategoryExportResponse> searchCategoriesWithoutPaging(CategorySearchRequest dto) {
        Pageable pageable = Pageable.unpaged();

        PaginationDTO<CategorySearchResponse> resultWrapper = searchCategories(dto, pageable);

        List<CategorySearchResponse> searchResponses = resultWrapper.getData();

        List<CategoryExportResponse> exportResponses = searchResponses.stream()
                .map(categoryExportMapper::toExportResponseFromSearchResponse)
                .collect(Collectors.toList());

        return exportResponses;
    }


}
