package com.mentalhealth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mentalhealth.entity.CrisisAlert;
import com.mentalhealth.entity.EmergencyContact;
import com.mentalhealth.service.CrisisAlertService;
import com.mentalhealth.service.EmergencyContactService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crisis")
public class CrisisAlertController {

    @Autowired
    private CrisisAlertService crisisAlertService;

    @Autowired
    private EmergencyContactService emergencyContactService;

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
        String status = (String) body.get("status");
        Long adminId = (Long) request.getAttribute("userId");
        crisisAlertService.handleAlert(id, adminId, remark, status);
        return ResultVO.success();
    }

    /** 获取预警用户的紧急联系人（管理员用） */
    @GetMapping("/{userId}/emergency-contacts")
    public ResultVO<List<EmergencyContact>> getEmergencyContacts(@PathVariable Long userId) {
        return ResultVO.success(emergencyContactService.getUserContacts(userId));
    }

    /** 标记已通知（每次通知一位联系人，追加到通知记录） */
    @PostMapping("/notify")
    public ResultVO<?> notifyGuardian(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        CrisisAlert alert = crisisAlertService.getById(id);
        if (alert != null) {
            alert.setGuardianNotified(1);

            // 通知时间
            java.time.LocalDateTime notifyTime;
            try {
                String t = (String) body.get("notifyTime");
                notifyTime = t != null ? java.time.LocalDateTime.parse(t) : java.time.LocalDateTime.now();
            } catch (Exception e) {
                notifyTime = java.time.LocalDateTime.now();
            }
            alert.setGuardianNotifyTime(notifyTime);

            // 通知方式
            String method = (String) body.get("notifyMethod");
            if (method != null) alert.setGuardianNotifyMethod(method);

            // 被通知联系人
            String contactName = (String) body.get("contactName");
            String contactRelation = (String) body.get("contactRelation");
            String contactPhone = (String) body.get("contactPhone");
            String remark = (String) body.get("notifyRemark");

            // 构成本次通知记录
            String timeStr = notifyTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
            String entry = "[" + timeStr + "] " + (method != null ? method : "") + " → "
                    + (contactName != null ? contactName : "") + "("
                    + (contactRelation != null ? contactRelation + "/" : "")
                    + (contactPhone != null ? contactPhone : "") + ")"
                    + (remark != null && !remark.isEmpty() ? " | " + remark : "");

            // 追加到已有记录
            String existing = alert.getGuardianNotifyRemark();
            if (existing != null && !existing.isEmpty()) {
                alert.setGuardianNotifyRemark(entry + "\n" + existing);
            } else {
                alert.setGuardianNotifyRemark(entry);
            }

            crisisAlertService.updateById(alert);
        }
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
