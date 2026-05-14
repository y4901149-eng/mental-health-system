package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.User;
import com.mentalhealth.vo.UserVO;

/**
 * 用户 Service 接口
 * 作用：定义用户相关的业务方法
 */
public interface UserService extends IService<User> {

    /** 用户登录，成功返回带 token 的 UserVO */
    UserVO login(String username, String password);

    /** 用户注册 */
    UserVO register(String username, String password, String nickname, String email);

    /** 根据用户名查询用户 */
    User getByUsername(String username);

    /** 根据用户ID获取用户视图信息 */
    UserVO getUserVOById(Long id);

    /** 更新用户个人信息 */
    void updateProfile(User user);

    /** 修改密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
