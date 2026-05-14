package com.mentalhealth.vo;

import lombok.Data;

/**
 * 用户视图对象
 * 作用：返回给前端的用户信息（不包含密码等敏感字段）
 */
@Data
public class UserVO {
    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 角色 */
    private String role;

    /** 头像 */
    private String avatar;

    /** 性别 */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 职业 */
    private String occupation;

    /** 个人简介 */
    private String bio;

    /** JWT令牌（登录时返回） */
    private String token;
}
