package com.example.baitapcuoiki2.mapper;

import com.example.baitapcuoiki2.dto.response.PaginationDTO;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface PaginationMapper {
    default <T> PaginationDTO<T> toPaginationDTO(Page<T> page) {
        return new PaginationDTO<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
