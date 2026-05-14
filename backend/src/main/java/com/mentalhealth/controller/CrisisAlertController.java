package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentalhealth.entity.CrisisAlert;
import com.mentalhealth.service.CrisisAlertService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/crisis")
public class CrisisAlertController {

    @Autowired
    private CrisisAlertService crisisAlertService;

    @GetMapping("/list")
    public ResultVO<IPage<CrisisAlert>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        return ResultVO.success(crisisAlertService.getAlertPage(pageNum, pageSize, status, userId));
    }

    @PostMapping("/handle")
    public ResultVO<?> handle(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long id = Long.valueOf(body.get("id").toString());
        String remark = (String) body.get("remark");
        Long adminId = (Long) request.getAttribute("userId");
        crisisAlertService.handleAlert(id, adminId, remark);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        crisisAlertService.removeById(id);
        return ResultVO.success();
    }

    @PostMapping("/create")
    public ResultVO<?> create(@RequestBody CrisisAlert alert, HttpServletRequest request) {
        alert.setUserId((Long) request.getAttribute("userId"));
        crisisAlertService.createAlert(alert);
        return ResultVO.success();
    }
}
