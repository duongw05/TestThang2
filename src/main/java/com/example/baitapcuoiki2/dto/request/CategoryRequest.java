package com.example.baitapcuoiki2.dto.request;

import com.example.baitapcuoiki2.exception.CustomException.UniqueCategoryCode;
import com.example.baitapcuoiki2.exception.CustomException.ValidCategoryImages;
import com.example.baitapcuoiki2.validations.Validations;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "{category.name.blank}", groups = {Validations.create.class})
    @Size(max = 100, message = "{category.name.size}", groups = {Validations.create.class, Validations.update.class})
    private String categoryName;

    @NotBlank(message = "{category.code.blank}", groups = {Validations.create.class})
    @Size(max = 50, message = "{category.code.size}", groups = {Validations.create.class})
    @UniqueCategoryCode(groups = {Validations.create.class})
    private String categoryCode;

    @NotBlank(message = "{category.description.notBlank}", groups = {Validations.create.class})
    @Size(max = 200, message = "{category.description.size}", groups = {Validations.create.class, Validations.update.class})
    private String description;

    private String status;

    private List<Long> oldImageIds;

    @ValidCategoryImages(groups = Validations.create.class,message = "{categoryRequest.categoryImages.NotNull}")
    private List<MultipartFile> categoryImages;

}
