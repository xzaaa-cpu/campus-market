package com.campus.market.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 30, message = "分类名称最多 30 个字")
    private String name;

    @Size(max = 20, message = "图标最多 20 个字符")
    private String icon;

    private Integer sort;
}
