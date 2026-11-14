package com.campus.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BizException;
import com.campus.market.dto.LoginRequest;
import com.campus.market.dto.RegisterRequest;
import com.campus.market.entity.User;
import com.campus.market.mapper.UserMapper;
import com.campus.market.util.JwtUtil;
import com.campus.market.util.PasswordUtil;
import com.campus.market.vo.AuthVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthVO register(RegisterRequest req) {
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (exist != null) {
            throw new BizException("用户名已被占用，换一个试试");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(PasswordUtil.encode(req.getPassword()));
        user.setNickname(StringUtils.hasText(req.getNickname()) ? req.getNickname() : req.getUsername());
        user.setIntro("这个同学很懒，还没有写简介");
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);

        return new AuthVO(jwtUtil.generate(user.getId(), user.getUsername(), user.getRole()), UserService.toVO(user));
    }

    public AuthVO login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (user == null || !PasswordUtil.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(401, "用户名或密码不正确");
        }
        return new AuthVO(jwtUtil.generate(user.getId(), user.getUsername(), user.getRole()), UserService.toVO(user));
    }
}
