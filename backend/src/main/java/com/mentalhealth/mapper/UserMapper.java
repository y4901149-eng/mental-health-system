package com.mentalhealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mentalhealth.entity.User;

/**
 * 用户 Mapper 接口
 * 作用：MyBatis-Plus 自动实现 User 表的 CRUD，无需写基础 SQL
 * 扩展：可在此添加自定义查询方法
 */
public interface UserMapper extends BaseMapper<User> {
}
