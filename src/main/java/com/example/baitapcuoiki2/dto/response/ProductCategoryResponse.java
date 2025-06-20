package com.example.baitapcuoiki2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductCategoryResponse {
    private Long productId;
    private Long categoryId;
    private Date createdDate;
    private Date modifiedDate;
}
