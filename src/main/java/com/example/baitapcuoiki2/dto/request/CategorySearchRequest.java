package com.example.baitapcuoiki2.dto.request;

import com.example.baitapcuoiki2.exception.CustomException.ValidDateRange;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ValidDateRange(message = "{validDateRange.invalid}")
public class CategorySearchRequest {

    @Size(max = 100, message = "{categorySearch.keyword.maxSize}")
    private String keyword;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @PastOrPresent(message = "{createdFrom.pastOrPresent}")
    private Date createdFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @PastOrPresent(message = "{createdTo.pastOrPresent}")
    private Date createdTo;

    @Min(value = 0, message = "{pagination.page.min}")
    private Integer page ;

    @Min(value = 1, message = "{pagination.size.min}")
    @Max(value = 100, message = "{pagination.size.max}")
    private Integer size;


}
