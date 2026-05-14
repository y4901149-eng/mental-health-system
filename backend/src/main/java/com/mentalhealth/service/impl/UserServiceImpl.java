package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.UserMapper;
import com.mentalhealth.service.UserService;
import com.mentalhealth.utils.JwtUtil;
import com.mentalhealth.utils.PasswordEncoder;
import com.mentalhealth.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户 Service 实现类
 * 作用：实现登录、注册等用户相关业务逻辑
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserVO login(String username, String password) {
        // 1. 根据用户名查询用户
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成 token 并返回用户信息
        return buildUserVO(user);
    }

    @Override
    public UserVO register(String username, String password, String nickname, String email) {
        // 1. 检查用户名是否已存在
        if (getByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setEmail(email);
        user.setRole("user");   // 默认普通用户
        user.setGender(0);      // 默认未知
        user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");

        // 3. 保存到数据库
        save(user);

        // 4. 返回带 token 的用户信息
        return buildUserVO(user);
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public UserVO getUserVOById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return buildUserVO(user);
    }

    @Override
    public void updateProfile(User user) {
        // 只更新允许修改的字段
        User existing = getById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        // 更新非空字段
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getGender() != null) existing.setGender(user.getGender());
        if (user.getAge() != null) existing.setAge(user.getAge());
        if (user.getBio() != null) existing.setBio(user.getBio());
        if (user.getOccupation() != null) existing.setOccupation(user.getOccupation());
        if (user.getAvatar() != null) existing.setAvatar(user.getAvatar());
        updateById(existing);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    /** 构建带 token 的用户视图对象 */
    private UserVO buildUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        userVO.setToken(token);
        return userVO;
    }
}
