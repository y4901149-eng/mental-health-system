package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.User;
import com.mentalhealth.vo.UserVO;

public interface UserService extends IService<User> {

    UserVO login(String username, String password);

    UserVO register(String username, String password, String nickname, String email);

    User getByUsername(String username);

    UserVO getUserVOById(Long id);

    void updateProfile(User user);

    UserVO updateAvatar(Long userId, String avatar);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
