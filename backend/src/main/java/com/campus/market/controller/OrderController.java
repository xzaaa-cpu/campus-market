package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.config.CurrentUser;
import com.campus.market.config.RequireLogin;
import com.campus.market.dto.OrderCreateRequest;
import com.campus.market.service.OrderService;
import com.campus.market.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequireLogin
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateRequest request) {
        return Result.ok(orderService.create(CurrentUser.requireUserId(), request.getItemId()));
    }

    @GetMapping
    public Result<List<OrderVO>> list(@RequestParam(defaultValue = "bought") String role) {
        return Result.ok(orderService.list(CurrentUser.requireUserId(), role));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id, CurrentUser.requireUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(id, CurrentUser.requireUserId());
        return Result.ok();
    }
}
