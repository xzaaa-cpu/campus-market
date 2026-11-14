package com.campus.market.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象，冗余商品与买卖双方信息，前端可直接渲染。
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    private Long itemId;
    private String itemTitle;
    private String itemImage;
    private BigDecimal itemPrice;

    private Long buyerId;
    private String buyerNickname;
    private Long sellerId;
    private String sellerNickname;
}
