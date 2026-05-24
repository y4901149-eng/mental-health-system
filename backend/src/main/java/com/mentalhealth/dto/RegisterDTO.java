package com.mentalhealth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 注册数据传输对象
 * 作用：接收前端传来的注册请求参数，校验用户名长度和密码强度
 */
@Data
public class RegisterDTO {
    /** 用户名 - 2-20位 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度需在2-20之间")
    private String username;

    /** 密码 - 最少6位 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度需在6-40之间")
    private String password;

    /** 确认密码 */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;
}
