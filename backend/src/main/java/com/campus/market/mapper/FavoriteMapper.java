package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.entity.Favorite;
import com.campus.market.vo.ItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 我的收藏（联表带出商品信息）。
     */
    List<ItemVO> selectFavoriteItems(@Param("userId") Long userId);
}
