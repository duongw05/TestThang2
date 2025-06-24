package com.example.baitapcuoiki2.dto.request;

import com.example.baitapcuoiki2.exception.CustomException.ProductValidateRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
@ProductValidateRange(message = "{validDateRange.invalid}")
public class ProductSearchRequest {

    @Size(max = 100, message = "{productSearch.keyword.maxSize}")
    private String keyword;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @PastOrPresent(message = "{createdFrom.pastOrPresent}")
    private Date createdFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @PastOrPresent(message = "{createdTo.pastOrPresent}")
    private Date createdTo;

    private Long categoryIds;

    @Min(value = 0, message = "{pagination.page.min}")
    private Integer page ;

    @Min(value = 1, message = "{pagination.size.min}")
    @Max(value = 100, message = "{pagination.size.max}")
    private Integer size;

}
