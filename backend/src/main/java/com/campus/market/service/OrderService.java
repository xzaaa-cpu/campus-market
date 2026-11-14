package com.campus.market.service;

import com.campus.market.common.BizException;
import com.campus.market.entity.Item;
import com.campus.market.entity.TradeOrder;
import com.campus.market.entity.User;
import com.campus.market.mapper.ItemMapper;
import com.campus.market.mapper.OrderMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Transactional
    public OrderVO create(Long buyerId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BizException(404, "商品不存在");
        }
        if (item.getStatus() != Item.STATUS_ON_SALE) {
            throw new BizException("该商品已下架或正在交易中");
        }
        if (item.getSellerId().equals(buyerId)) {
            throw new BizException("不能购买自己发布的商品");
        }

        // 标记商品为交易中
        item.setStatus(Item.STATUS_TRADING);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);

        TradeOrder order = new TradeOrder();
        order.setOrderNo(generateOrderNo());
        order.setItemId(item.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(item.getSellerId());
        order.setAmount(item.getPrice() == null ? BigDecimal.ZERO : item.getPrice());
        order.setStatus(TradeOrder.STATUS_PENDING);
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        return toVO(order, item);
    }

    @Transactional
    public void cancel(Long orderId, Long userId) {
        TradeOrder order = requireOrder(orderId);
        if (order.getStatus() != TradeOrder.STATUS_PENDING) {
            throw new BizException("该订单当前状态不支持取消");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BizException(403, "无权操作该订单");
        }

        order.setStatus(TradeOrder.STATUS_CANCELED);
        order.setFinishedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        Item item = itemMapper.selectById(order.getItemId());
        if (item != null && item.getStatus() == Item.STATUS_TRADING) {
            item.setStatus(Item.STATUS_ON_SALE);
            item.setUpdatedAt(LocalDateTime.now());
            itemMapper.updateById(item);
        }
    }

    @Transactional
    public void confirm(Long orderId, Long buyerId) {
        TradeOrder order = requireOrder(orderId);
        if (order.getStatus() != TradeOrder.STATUS_PENDING) {
            throw new BizException("该订单当前状态不支持确认收货");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BizException(403, "只有买家可以确认收货");
        }

        order.setStatus(TradeOrder.STATUS_FINISHED);
        order.setFinishedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        Item item = itemMapper.selectById(order.getItemId());
        if (item != null) {
            item.setStatus(Item.STATUS_SOLD);
            item.setUpdatedAt(LocalDateTime.now());
            itemMapper.updateById(item);
        }
    }

    public List<OrderVO> list(Long userId, String role) {
        return orderMapper.selectOrderList(userId, "sold".equals(role) ? "sold" : "bought");
    }

    private TradeOrder requireOrder(Long orderId) {
        TradeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        return order;
    }

    private OrderVO toVO(TradeOrder order, Item item) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setFinishedAt(order.getFinishedAt());
        vo.setItemId(order.getItemId());
        vo.setItemTitle(item.getTitle());
        vo.setItemImage(item.getImageUrl());
        vo.setItemPrice(item.getPrice());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());

        User buyer = userMapper.selectById(order.getBuyerId());
        User seller = userMapper.selectById(order.getSellerId());
        vo.setBuyerNickname(buyer == null ? null : buyer.getNickname());
        vo.setSellerNickname(seller == null ? null : seller.getNickname());
        return vo;
    }

    private String generateOrderNo() {
        return "CM" + LocalDateTime.now().format(NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
