package com.mentalhealth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 登录数据传输对象
 * 作用：接收前端传来的登录请求参数，自动校验非空
 */
@Data
public class LoginDTO {
    /** 用户名 - 不能为空 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 - 不能为空 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
