package com.campus.market.dto;

import lombok.Data;

/**
 * 商品列表查询条件。
 */
@Data
public class ItemQuery {

    private String keyword;

    private Long categoryId;

    /** latest / priceAsc / priceDesc / popular */
    private String sort = "latest";

    private Integer page = 1;

    private Integer size = 12;
}
