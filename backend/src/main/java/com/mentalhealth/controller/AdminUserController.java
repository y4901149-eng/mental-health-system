package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.entity.User;
import com.mentalhealth.service.UserService;
import com.mentalhealth.utils.PasswordEncoder;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResultVO<IPage<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "15") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getId);
        if (userId != null) {
            wrapper.eq(User::getId, userId);
        } else if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getEmail, keyword));
        }
        Page<User> page = new Page<>(pageNum, pageSize);
        // 修复分页 total=0 的问题：关闭优化 count SQL
        page.setOptimizeCountSql(false);
        userService.page(page, wrapper);
        // 如果 total 仍为 0 但 records 有数据，手动设置 total
        if (page.getTotal() == 0 && page.getRecords() != null && !page.getRecords().isEmpty()) {
            page.setTotal((long) page.getRecords().size());
        }
        return ResultVO.success(page);
    }

    @PostMapping
    public ResultVO<?> create(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return ResultVO.badRequest("用户名和密码不能为空");
        }
        if (userService.getByUsername(user.getUsername()) != null) {
            return ResultVO.badRequest("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("user");
        if (user.getGender() == null) user.setGender(0);
        user.setStatus(1);
        userService.save(user);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    public ResultVO<?> update(@PathVariable Long id, @RequestBody User user) {
        User existing = userService.getById(id);
        if (existing == null) return ResultVO.badRequest("用户不存在");
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getGender() != null) existing.setGender(user.getGender());
        if (user.getAge() != null) existing.setAge(user.getAge());
        if (user.getOccupation() != null) existing.setOccupation(user.getOccupation());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getRole() != null) existing.setRole(user.getRole());
        if (user.getStatus() != null) existing.setStatus(user.getStatus());
        userService.updateById(existing);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return ResultVO.success();
    }

    @PostMapping("/{id}/reset-password")
    public ResultVO<?> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return ResultVO.badRequest("用户不存在");
        user.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(user);
        return ResultVO.success();
    }

    @PostMapping("/{id}/toggle-status")
    public ResultVO<?> toggleStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return ResultVO.badRequest("用户不存在");
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userService.updateById(user);
        return ResultVO.success();
    }
}
