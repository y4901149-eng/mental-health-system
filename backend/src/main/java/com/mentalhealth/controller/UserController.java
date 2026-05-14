package com.mentalhealth.controller;

import com.mentalhealth.dto.LoginDTO;
import com.mentalhealth.dto.RegisterDTO;
import com.mentalhealth.entity.User;
import com.mentalhealth.service.UserService;
import com.mentalhealth.vo.ResultVO;
import com.mentalhealth.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 用户 Controller
 * 作用：处理登录、注册、获取用户信息等请求
 * 路径：/api/user/*
 * 说明：登录和注册不拦截（在 WebMvcConfig 中排除了），其他接口需要 JWT token
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * POST /api/user/login
     * 请求体：{ "username": "xxx", "password": "xxx" }
     * 返回：{ code: 200, data: { id, username, nickname, role, token } }
     */
    @PostMapping("/login")
    public ResultVO<UserVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return ResultVO.success(userVO);
    }

    /**
     * 用户注册
     * POST /api/user/register
     * 请求体：{ "username": "xxx", "password": "xxx", "confirmPassword": "xxx", "nickname": "xxx" }
     */
    @PostMapping("/register")
    public ResultVO<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        // 检查两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return ResultVO.badRequest("两次密码不一致");
        }
        UserVO userVO = userService.register(
                registerDTO.getUsername(),
                registerDTO.getPassword(),
                registerDTO.getNickname(),
                registerDTO.getEmail()
        );
        return ResultVO.success(userVO);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/info
     * 请求头需要带 Authorization: Bearer xxx
     */
    @GetMapping("/info")
    public ResultVO<UserVO> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserVO userVO = userService.getUserVOById(userId);
        return ResultVO.success(userVO);
    }

    /**
     * 更新个人信息
     * PUT /api/user/update
     */
    @PutMapping("/update")
    public ResultVO<?> update(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        userService.updateProfile(user);
        return ResultVO.success();
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public ResultVO<?> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        if (oldPwd == null || newPwd == null) {
            return ResultVO.badRequest("密码不能为空");
        }
        userService.changePassword(userId, oldPwd, newPwd);
        return ResultVO.success();
    }
}
