package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单。status：1 待确认 / 2 已完成 / 3 已取消。
 */
@Data
@TableName("t_order")
public class TradeOrder {

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_CANCELED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long itemId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal amount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;
}
