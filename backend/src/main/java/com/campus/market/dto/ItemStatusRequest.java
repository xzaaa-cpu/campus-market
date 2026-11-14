package com.campus.market.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ItemStatusRequest {

    /** 1 在售 / 4 已下架 */
    @NotNull(message = "目标状态不能为空")
    private Integer status;
}
