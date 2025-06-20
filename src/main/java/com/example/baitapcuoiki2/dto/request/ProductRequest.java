package com.example.baitapcuoiki2.dto.request;

import com.example.baitapcuoiki2.exception.CustomException.NotEmptyLongList;
import com.example.baitapcuoiki2.exception.CustomException.UniqueProductCode;
import com.example.baitapcuoiki2.exception.CustomException.ValidImageMaxSize;
import com.example.baitapcuoiki2.exception.CustomException.ValidProductImages;
import com.example.baitapcuoiki2.validations.Validations;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductRequest {

    @NotBlank(groups = Validations.create.class, message = "{productRequest.productName.NotBlank}")
    @Size(max = 200, message = "{productRequest.productName.Size}",groups = {Validations.create.class, Validations.update.class})
    private String productName;

    @NotBlank(groups = Validations.create.class, message = "{productRequest.productCode.NotBlank}")
    @UniqueProductCode(message = "{product.code.duplicate}",groups = Validations.create.class)
    private String productCode;

    @NotBlank(groups = Validations.create.class,message = "{productRequest.description.NotBlank}")
    @Size(max = 200, message = "{productRequest.description.Size}",groups = {Validations.create.class, Validations.update.class})
    private String description;

    @NotNull(groups = Validations.create.class, message = "{productRequest.price.NotNull}")
    @DecimalMin(value = "0.0", groups = {Validations.create.class, Validations.update.class}, message = "{productRequest.price.DecimalMin}")
    private Double price;

    @NotNull(groups = Validations.create.class, message = "{productRequest.quantity.NotNull}")
    @Min(value = 0,groups = {Validations.create.class, Validations.update.class}, message = "{productRequest.quantity.Min}")
    private Long quantity;

    private String status;
    private Date createdDate;
    private Date modifiedDate;
    private String createdBy;
    private String modifiedBy;

    @NotEmptyLongList(groups = Validations.create.class, message = "{productRequest.categories.NotNull}")
    private List<Long> categories;

    private List<Long> oldImageIds;

    @ValidProductImages(groups = Validations.create.class, message = "{productRequest.productImages.NotNull}")
    @ValidImageMaxSize(maxSize = 3 * 1024 * 1024,message = "{productRequest.productImages.MaxSize}") // 3MB
    private List<MultipartFile> productImages;
}
