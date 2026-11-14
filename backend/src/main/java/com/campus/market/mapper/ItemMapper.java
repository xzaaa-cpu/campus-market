package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.dto.ItemQuery;
import com.campus.market.entity.Item;
import com.campus.market.vo.ItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 首页商品分页（联表带出分类名与卖家昵称）。
     */
    IPage<ItemVO> selectItemPage(Page<ItemVO> page, @Param("q") ItemQuery q);

    /**
     * 商品详情。
     */
    ItemVO selectItemDetail(@Param("id") Long id);

    /**
     * 我发布的商品（可按状态筛选）。
     */
    List<ItemVO> selectMyItems(@Param("sellerId") Long sellerId, @Param("status") Integer status);
}
