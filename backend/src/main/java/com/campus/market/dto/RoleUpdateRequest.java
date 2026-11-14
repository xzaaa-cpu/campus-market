package com.campus.market.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RoleUpdateRequest {

    /** USER / ADMIN */
    @NotBlank(message = "角色不能为空")
    private String role;
}
