package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.config.CurrentUser;
import com.campus.market.config.RequireLogin;
import com.campus.market.dto.ItemQuery;
import com.campus.market.dto.ItemRequest;
import com.campus.market.entity.Item;
import com.campus.market.service.ItemService;
import com.campus.market.vo.ItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Result<PageResult<ItemVO>> page(ItemQuery query) {
        return Result.ok(itemService.page(query));
    }

    @GetMapping("/mine")
    @RequireLogin
    public Result<List<ItemVO>> mine(@RequestParam(required = false) Integer status) {
        return Result.ok(itemService.myItems(CurrentUser.requireUserId(), status));
    }

    @GetMapping("/{id}")
    public Result<ItemVO> detail(@PathVariable Long id) {
        return Result.ok(itemService.detail(id, CurrentUser.getUserId()));
    }

    @PostMapping
    @RequireLogin
    public Result<ItemVO> create(@Valid @RequestBody ItemRequest request) {
        return Result.ok(itemService.create(CurrentUser.requireUserId(), request));
    }

    @PutMapping("/{id}")
    @RequireLogin
    public Result<ItemVO> update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return Result.ok(itemService.update(id, CurrentUser.requireUserId(), request));
    }

    @PostMapping("/{id}/off-shelf")
    @RequireLogin
    public Result<Void> offShelf(@PathVariable Long id) {
        itemService.changeStatus(id, CurrentUser.requireUserId(), Item.STATUS_OFF_SHELF);
        return Result.ok();
    }

    @PostMapping("/{id}/on-shelf")
    @RequireLogin
    public Result<Void> onShelf(@PathVariable Long id) {
        itemService.changeStatus(id, CurrentUser.requireUserId(), Item.STATUS_ON_SALE);
        return Result.ok();
    }
}
