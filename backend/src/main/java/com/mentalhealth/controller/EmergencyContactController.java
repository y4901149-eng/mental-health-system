package com.mentalhealth.controller;

import com.mentalhealth.entity.EmergencyContact;
import com.mentalhealth.service.EmergencyContactService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyContactController {

    @Autowired
    private EmergencyContactService emergencyContactService;

    @GetMapping("/list")
    public ResultVO<List<EmergencyContact>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(emergencyContactService.getUserContacts(userId));
    }

    @GetMapping("/default")
    public ResultVO<EmergencyContact> getDefault(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(emergencyContactService.getDefaultContact(userId));
    }

    @PostMapping("/create")
    public ResultVO<?> create(@RequestBody EmergencyContact contact, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contact.setUserId(userId);
        emergencyContactService.save(contact);
        return ResultVO.success();
    }

    @PutMapping("/update")
    public ResultVO<?> update(@RequestBody EmergencyContact contact, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        EmergencyContact existing = emergencyContactService.getById(contact.getId());
        if (existing != null && existing.getUserId().equals(userId)) {
            existing.setName(contact.getName());
            existing.setPhone(contact.getPhone());
            existing.setRelation(contact.getRelation());
            emergencyContactService.updateById(existing);
        }
        return ResultVO.success();
    }

    @DeleteMapping("/delete/{id}")
    public ResultVO<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        EmergencyContact contact = emergencyContactService.getById(id);
        if (contact != null && contact.getUserId().equals(userId)) {
            emergencyContactService.removeById(id);
        }
        return ResultVO.success();
    }

    @PostMapping("/default/{id}")
    public ResultVO<?> setDefault(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        emergencyContactService.setDefault(userId, id);
        return ResultVO.success();
    }
}
