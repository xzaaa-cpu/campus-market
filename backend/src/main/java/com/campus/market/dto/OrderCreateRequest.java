package com.campus.market.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderCreateRequest {

    @NotNull(message = "请选择要购买的商品")
    private Long itemId;
}
