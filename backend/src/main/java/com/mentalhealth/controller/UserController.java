package com.mentalhealth.controller;

import com.mentalhealth.dto.LoginDTO;
import com.mentalhealth.dto.RegisterDTO;
import com.mentalhealth.entity.User;
import com.mentalhealth.service.UserService;
import com.mentalhealth.vo.ResultVO;
import com.mentalhealth.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResultVO<UserVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResultVO.success(userService.login(loginDTO.getUsername(), loginDTO.getPassword()));
    }

    @PostMapping("/register")
    public ResultVO<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
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

    @GetMapping("/info")
    public ResultVO<UserVO> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(userService.getUserVOById(userId));
    }

    @PutMapping("/update")
    public ResultVO<UserVO> update(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        userService.updateProfile(user);
        return ResultVO.success(userService.getUserVOById(userId));
    }

    @PostMapping("/avatar")
    public ResultVO<UserVO> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResultVO.badRequest("请选择头像图片");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            return ResultVO.badRequest("头像图片不能超过2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResultVO.badRequest("只支持图片文件");
        }

        Long userId = (Long) request.getAttribute("userId");
        String dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
        return ResultVO.success(userService.updateAvatar(userId, dataUrl));
    }

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
