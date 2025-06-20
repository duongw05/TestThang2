package com.example.baitapcuoiki2.dto.response;

import com.example.baitapcuoiki2.utils.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class CategoryResponse {
    private Long id;
    private String categoryName;
    private String categoryCode;
    private String description;
    private Status status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;

    private List<CategoryImageResponse> categoryImages;

    public CategoryResponse(Long id, String categoryName, String categoryCode, String description, Status status, Date createdDate, Date modifiedDate, String createdBy, String modifiedBy) {
        this.id = id;
        this.categoryName = categoryName;
        this.categoryCode = categoryCode;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
    }
}
