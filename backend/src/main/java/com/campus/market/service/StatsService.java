package com.campus.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.entity.Item;
import com.campus.market.entity.TradeOrder;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.mapper.ItemMapper;
import com.campus.market.mapper.OrderMapper;
import com.campus.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首页数据概览。
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;

    public Map<String, Object> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("itemCount", itemMapper.selectCount(null));
        data.put("onSaleCount", itemMapper.selectCount(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, Item.STATUS_ON_SALE)));
        data.put("soldCount", itemMapper.selectCount(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, Item.STATUS_SOLD)));
        data.put("orderCount", orderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getStatus, TradeOrder.STATUS_FINISHED)));
        data.put("categoryCount", categoryMapper.selectCount(null));
        return data;
    }
}
