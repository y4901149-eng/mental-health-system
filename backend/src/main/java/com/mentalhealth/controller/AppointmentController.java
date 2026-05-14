package com.mentalhealth.controller;

import com.mentalhealth.entity.Appointment;
import com.mentalhealth.service.AppointmentService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 预约 Controller
 */
@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /** 创建预约 */
    @PostMapping("/create")
    public ResultVO<?> create(@RequestBody Appointment appointment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        appointment.setUserId(userId);
        appointmentService.createAppointment(appointment);
        return ResultVO.success();
    }

    /** 获取我的预约列表 */
    @GetMapping("/my")
    public ResultVO<List<Appointment>> myAppointments(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResultVO.success(appointmentService.getUserAppointments(userId));
    }

    /** 取消预约 */
    @PutMapping("/cancel/{id}")
    public ResultVO<?> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        appointmentService.cancelAppointment(id, userId);
        return ResultVO.success();
    }
}
