package com.campus.market.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象（不含密码）。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String intro;
    private String role;
    private LocalDateTime createdAt;
}
