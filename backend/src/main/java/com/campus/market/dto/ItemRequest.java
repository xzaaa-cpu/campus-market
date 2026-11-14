package com.campus.market.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class ItemRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题最多 100 个字")
    private String title;

    @Size(max = 2000, message = "商品描述最多 2000 个字")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    private String imageUrl;

    private String itemCondition;

    private String location;
}
