package com.campus.market.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserUpdateRequest {

    @Size(max = 20, message = "昵称最多 20 个字")
    private String nickname;

    @Size(max = 30, message = "手机号最多 30 位")
    private String phone;

    private String avatar;

    @Size(max = 255, message = "个人简介最多 255 个字")
    private String intro;
}
