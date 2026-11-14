package com.campus.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.dto.ItemQuery;
import com.campus.market.dto.ItemRequest;
import com.campus.market.entity.Category;
import com.campus.market.entity.Favorite;
import com.campus.market.entity.Item;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.mapper.FavoriteMapper;
import com.campus.market.mapper.ItemMapper;
import com.campus.market.vo.ItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;

    public PageResult<ItemVO> page(ItemQuery q) {
        long pageNo = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        long size = q.getSize() == null || q.getSize() < 1 ? 12 : Math.min(q.getSize(), 50);
        Page<ItemVO> page = new Page<>(pageNo, size);
        IPage<ItemVO> result = itemMapper.selectItemPage(page, q);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public ItemVO detail(Long itemId, Long currentUserId) {
        ItemVO vo = itemMapper.selectItemDetail(itemId);
        if (vo == null) {
            throw new BizException(404, "商品不存在");
        }

        itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                .eq(Item::getId, itemId)
                .setSql("view_count = view_count + 1"));
        vo.setViewCount((vo.getViewCount() == null ? 0 : vo.getViewCount()) + 1);

        boolean favorited = false;
        if (currentUserId != null) {
            favorited = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, currentUserId)
                    .eq(Favorite::getItemId, itemId)) > 0;
        }
        vo.setFavorited(favorited);
        return vo;
    }

    @Transactional
    public ItemVO create(Long sellerId, ItemRequest req) {
        checkCategory(req.getCategoryId());

        Item item = new Item();
        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setOriginalPrice(req.getOriginalPrice());
        item.setCategoryId(req.getCategoryId());
        item.setSellerId(sellerId);
        item.setImageUrl(req.getImageUrl());
        item.setItemCondition(StringUtils.hasText(req.getItemCondition()) ? req.getItemCondition() : "几乎全新");
        item.setLocation(StringUtils.hasText(req.getLocation()) ? req.getLocation() : "校内面交");
        item.setStatus(Item.STATUS_ON_SALE);
        item.setViewCount(0);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.insert(item);

        return itemMapper.selectItemDetail(item.getId());
    }

    @Transactional
    public ItemVO update(Long itemId, Long userId, ItemRequest req) {
        Item item = requireOwnItem(itemId, userId);
        if (item.getStatus() == Item.STATUS_TRADING || item.getStatus() == Item.STATUS_SOLD) {
            throw new BizException("交易中或已售出的商品不支持修改");
        }
        checkCategory(req.getCategoryId());

        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setOriginalPrice(req.getOriginalPrice());
        item.setCategoryId(req.getCategoryId());
        item.setImageUrl(req.getImageUrl());
        item.setItemCondition(req.getItemCondition());
        item.setLocation(req.getLocation());
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);

        return itemMapper.selectItemDetail(itemId);
    }

    @Transactional
    public void changeStatus(Long itemId, Long userId, int targetStatus) {
        Item item = requireOwnItem(itemId, userId);
        if (item.getStatus() == Item.STATUS_TRADING || item.getStatus() == Item.STATUS_SOLD) {
            throw new BizException("交易中或已售出的商品不能变更状态");
        }
        item.setStatus(targetStatus);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    public List<ItemVO> myItems(Long sellerId, Integer status) {
        return itemMapper.selectMyItems(sellerId, status);
    }

    private Item requireOwnItem(Long itemId, Long userId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BizException(404, "商品不存在");
        }
        if (!item.getSellerId().equals(userId)) {
            throw new BizException(403, "只能操作自己发布的商品");
        }
        return item;
    }

    private void checkCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException("商品分类不存在");
        }
    }
}
