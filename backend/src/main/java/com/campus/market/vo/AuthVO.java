package com.campus.market.vo;

import lombok.Data;

/**
 * 登录 / 注册返回：token + 用户信息。
 */
@Data
public class AuthVO {

    private String token;
    private UserVO user;

    public AuthVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }
}
