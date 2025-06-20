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
public class ProductExportResponse {
    private Integer id;
    private String productName;
    private String productCode;
    private Double price;
    private Long quantity;
    private Date createdDate;
    private Date modifiedDate;
    private List<String> categoryNames;
}
