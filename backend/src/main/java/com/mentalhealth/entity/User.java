package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 作用：对应数据库 user 表，存储用户基本信息
 */
@Data
@TableName("user")
public class User {
    /** 用户ID - 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 - 登录唯一标识 */
    private String username;

    /** 密码 - BCrypt加密存储 */
    private String password;

    /** 昵称 - 显示名称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 角色: user-普通用户, admin-管理员 */
    private String role;

    /** 头像URL */
    private String avatar;

    /** 性别: 0-未知, 1-男, 2-女 */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 职业 */
    private String occupation;

    /** 个人简介 */
    private String bio;

    /** 状态: 1-正常, 0-禁用 */
    private Integer status;

    /** 逻辑删除标记: 0-未删除, 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 - 自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 - 自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
