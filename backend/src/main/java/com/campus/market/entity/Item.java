package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品（二手物品）。status：1 在售 / 2 交易中 / 3 已售出 / 4 已下架。
 */
@Data
@TableName("t_item")
public class Item {

    public static final int STATUS_ON_SALE = 1;
    public static final int STATUS_TRADING = 2;
    public static final int STATUS_SOLD = 3;
    public static final int STATUS_OFF_SHELF = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Long categoryId;

    private Long sellerId;

    private String imageUrl;

    private String itemCondition;

    private String location;

    private Integer status;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
