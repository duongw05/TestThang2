package com.example.baitapcuoiki2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductResponse {
    private Integer id;
    private String productName;
    private String productCode;
    private String description;
    private Double price;
    private Long quantity;
    private String status;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;
    private List<CategoryResponse> categories;
    private List<ProductImageResponse> productImages;

}
