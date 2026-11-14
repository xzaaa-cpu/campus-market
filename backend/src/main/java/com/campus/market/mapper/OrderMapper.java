package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.entity.TradeOrder;
import com.campus.market.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<TradeOrder> {

    /**
     * 我的订单列表。role：bought=我买到的，sold=我卖出的。
     */
    List<OrderVO> selectOrderList(@Param("userId") Long userId, @Param("role") String role);

    /**
     * 管理后台订单分页（可按状态筛选，全站数据）。
     */
    IPage<OrderVO> selectAdminOrderPage(Page<OrderVO> page, @Param("status") Integer status);
}
