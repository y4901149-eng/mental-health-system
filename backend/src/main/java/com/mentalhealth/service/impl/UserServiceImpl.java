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

import java.util.regex.Pattern;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public UserVO login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        return buildUserVO(user);
    }

    @Override
    public UserVO register(String username, String password, String nickname, String email) {
        if (getByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new RuntimeException("邮箱格式不正确");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname.trim() : username);
        user.setEmail(normalize(email));
        user.setRole("user");
        user.setGender(0);
        user.setStatus(1);
        user.setAvatar("");
        save(user);
        return buildUserVO(user);
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
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
        User existing = getById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getNickname() != null) {
            String nickname = user.getNickname().trim();
            if (nickname.isEmpty() || nickname.length() > 30) {
                throw new RuntimeException("昵称长度需在1-30个字符之间");
            }
            existing.setNickname(nickname);
        }
        if (user.getEmail() != null) {
            String email = normalize(user.getEmail());
            if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
                throw new RuntimeException("邮箱格式不正确");
            }
            existing.setEmail(email);
        }
        if (user.getPhone() != null) {
            String phone = normalize(user.getPhone());
            if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
                throw new RuntimeException("手机号格式不正确");
            }
            existing.setPhone(phone);
        }
        if (user.getGender() != null) {
            if (user.getGender() < 0 || user.getGender() > 2) {
                throw new RuntimeException("性别参数不正确");
            }
            existing.setGender(user.getGender());
        }
        if (user.getAge() != null) {
            if (user.getAge() < 1 || user.getAge() > 120) {
                throw new RuntimeException("年龄需在1-120之间");
            }
            existing.setAge(user.getAge());
        }
        if (user.getBio() != null) {
            String bio = normalize(user.getBio());
            if (bio != null && bio.length() > 200) {
                throw new RuntimeException("个人简介不能超过200个字符");
            }
            existing.setBio(bio);
        }
        if (user.getOccupation() != null) {
            String occupation = normalize(user.getOccupation());
            if (occupation != null && occupation.length() > 50) {
                throw new RuntimeException("职业不能超过50个字符");
            }
            existing.setOccupation(occupation);
        }
        if (user.getAvatar() != null) {
            existing.setAvatar(user.getAvatar());
        }
        updateById(existing);
    }

    @Override
    public UserVO updateAvatar(Long userId, String avatar) {
        User existing = getById(userId);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        existing.setAvatar(avatar);
        updateById(existing);
        return buildUserVO(existing);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 40) {
            throw new RuntimeException("新密码长度需在6-40位之间");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    private UserVO buildUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(jwtUtil.generateToken(user.getId(), user.getRole()));
        return userVO;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
