package com.campus.market.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品视图对象（列表 / 详情共用）。
 */
@Data
public class ItemVO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private Long sellerId;
    private String sellerNickname;
    private String sellerAvatar;
    private String imageUrl;
    private String itemCondition;
    private String location;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;

    /** 详情接口额外返回：当前用户是否已收藏 */
    private Boolean favorited;
}
