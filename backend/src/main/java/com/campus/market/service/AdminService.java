package com.campus.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.dto.CategoryRequest;
import com.campus.market.entity.Category;
import com.campus.market.entity.Item;
import com.campus.market.entity.TradeOrder;
import com.campus.market.entity.User;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.mapper.ItemMapper;
import com.campus.market.mapper.OrderMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.vo.ItemVO;
import com.campus.market.vo.OrderVO;
import com.campus.market.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台业务：数据概览、用户与权限、商品审核、全站订单、分类维护。
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MM-dd");

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final OrderMapper orderMapper;
    private final CategoryMapper categoryMapper;
    private final JdbcTemplate jdbcTemplate;

    // ---------------- 概览 ----------------

    public Map<String, Object> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("adminCount", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN")));
        data.put("itemCount", itemMapper.selectCount(null));
        data.put("onSaleCount", countItems(Item.STATUS_ON_SALE));
        data.put("tradingCount", countItems(Item.STATUS_TRADING));
        data.put("soldCount", countItems(Item.STATUS_SOLD));
        data.put("offShelfCount", countItems(Item.STATUS_OFF_SHELF));
        data.put("orderCount", orderMapper.selectCount(null));
        data.put("finishedOrderCount", orderMapper.selectCount(
                new LambdaQueryWrapper<TradeOrder>().eq(TradeOrder::getStatus, TradeOrder.STATUS_FINISHED)));
        data.put("gmv", jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM t_order WHERE status = 2", BigDecimal.class));
        data.put("trend", trend());
        return data;
    }

    private long countItems(int status) {
        return itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getStatus, status));
    }

    /**
     * 最近 7 天的新增商品与成交订单数量。
     */
    private List<Map<String, Object>> trend() {
        LocalDate today = LocalDate.now();
        LocalDate since = today.minusDays(6);
        Map<String, Long> itemMap = countByDay("t_item", since);
        Map<String, Long> orderMap = countByDay("t_order", since, "status = 2");

        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", day.format(DAY_LABEL));
            row.put("items", itemMap.getOrDefault(day.toString(), 0L));
            row.put("orders", orderMap.getOrDefault(day.toString(), 0L));
            list.add(row);
        }
        return list;
    }

    private Map<String, Long> countByDay(String table, LocalDate since) {
        return countByDay(table, since, null);
    }

    private Map<String, Long> countByDay(String table, LocalDate since, String extraCondition) {
        String sql = "SELECT CAST(created_at AS DATE) AS d, COUNT(*) AS c FROM " + table
                + " WHERE created_at >= ?" + (extraCondition == null ? "" : " AND " + extraCondition)
                + " GROUP BY CAST(created_at AS DATE)";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, Timestamp.valueOf(since.atStartOfDay()));
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object day = row.get("d");
            Number count = (Number) row.get("c");
            map.put(String.valueOf(day), count == null ? 0L : count.longValue());
        }
        return map;
    }

    // ---------------- 用户 ----------------

    public PageResult<UserVO> users(String keyword, String role, int page, int size) {
        Page<User> pageRequest = new Page<>(normalizePage(page), normalizeSize(size));
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> result = userMapper.selectPage(pageRequest, wrapper);
        List<UserVO> list = result.getRecords().stream()
                .map(UserService::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), list);
    }

    @Transactional
    public UserVO updateRole(Long operatorId, Long targetId, String role) {
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new BizException("角色只能是 USER 或 ADMIN");
        }
        if (operatorId.equals(targetId)) {
            throw new BizException("不能修改自己的角色，请让其他管理员操作");
        }
        User user = userMapper.selectById(targetId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        user.setRole(role);
        userMapper.updateById(user);
        return UserService.toVO(user);
    }

    // ---------------- 商品 ----------------

    public PageResult<ItemVO> items(String keyword, Integer status, int page, int size) {
        Page<ItemVO> pageRequest = new Page<>(normalizePage(page), normalizeSize(size));
        IPage<ItemVO> result = itemMapper.selectAdminItemPage(pageRequest, keyword, status);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    @Transactional
    public void changeItemStatus(Long itemId, Integer status) {
        if (status == null || (status != Item.STATUS_ON_SALE && status != Item.STATUS_OFF_SHELF)) {
            throw new BizException("管理员只能将商品设置为在售或下架");
        }
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BizException(404, "商品不存在");
        }
        if (item.getStatus() == Item.STATUS_TRADING || item.getStatus() == Item.STATUS_SOLD) {
            throw new BizException("交易中或已售出的商品不支持手工调整状态");
        }
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);
    }

    // ---------------- 订单 ----------------

    public PageResult<OrderVO> orders(Integer status, int page, int size) {
        Page<OrderVO> pageRequest = new Page<>(normalizePage(page), normalizeSize(size));
        IPage<OrderVO> result = orderMapper.selectAdminOrderPage(pageRequest, status);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    // ---------------- 分类 ----------------

    public List<Category> categories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
    }

    @Transactional
    public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setIcon(StringUtils.hasText(request.getIcon()) ? request.getIcon() : "✨");
        category.setSort(request.getSort() == null ? 99 : request.getSort());
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException(404, "分类不存在");
        }
        category.setName(request.getName());
        if (StringUtils.hasText(request.getIcon())) {
            category.setIcon(request.getIcon());
        }
        if (request.getSort() != null) {
            category.setSort(request.getSort());
        }
        categoryMapper.updateById(category);
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException(404, "分类不存在");
        }
        long used = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getCategoryId, id));
        if (used > 0) {
            throw new BizException("该分类下还有 " + used + " 件商品，请先调整商品分类");
        }
        categoryMapper.deleteById(id);
    }

    private long normalizePage(int page) {
        return page < 1 ? 1 : page;
    }

    private long normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
