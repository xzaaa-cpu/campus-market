package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.config.CurrentUser;
import com.campus.market.config.RequireLogin;
import com.campus.market.service.FavoriteService;
import com.campus.market.vo.ItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequireLogin
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Result<List<ItemVO>> list() {
        return Result.ok(favoriteService.list(CurrentUser.requireUserId()));
    }

    @PostMapping("/{itemId}")
    public Result<Void> add(@PathVariable Long itemId) {
        favoriteService.add(CurrentUser.requireUserId(), itemId);
        return Result.ok();
    }

    @DeleteMapping("/{itemId}")
    public Result<Void> remove(@PathVariable Long itemId) {
        favoriteService.remove(CurrentUser.requireUserId(), itemId);
        return Result.ok();
    }
}
