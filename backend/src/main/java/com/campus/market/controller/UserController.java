package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.config.CurrentUser;
import com.campus.market.config.RequireLogin;
import com.campus.market.dto.UserUpdateRequest;
import com.campus.market.service.UserService;
import com.campus.market.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @RequireLogin
    public Result<UserVO> me() {
        return Result.ok(userService.getById(CurrentUser.requireUserId()));
    }

    @PutMapping("/me")
    @RequireLogin
    public Result<UserVO> update(@Valid @RequestBody UserUpdateRequest request) {
        return Result.ok(userService.update(CurrentUser.requireUserId(), request));
    }
}
