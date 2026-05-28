package com.furniro.MessageService.dto.req.promotion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotionReq {
    
    private Integer id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 2, max = 100, message = "Tiêu đề phải từ 2-100 ký tự")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 2, max = 500, message = "Mô tả phải từ 2-500 ký tự")
    private String description;

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(min = 2, max = 50, message = "Mã khuyến mãi phải từ 2-50 ký tự")
    private String code;

    @NotBlank(message = "Loại khuyến mãi không được để trống")
    @Size(min = 2, max = 50, message = "Loại khuyến mãi phải từ 2-50 ký tự")
    private String type;

    @NotNull(message = "Giá trị khuyến mãi không được để trống")
    private Double value;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean status;
}
