package com.mentalhealth.controller;

import com.mentalhealth.entity.Appointment;
import com.mentalhealth.entity.Counselor;
import com.mentalhealth.entity.CounselorAvailableSlot;
import com.mentalhealth.service.AppointmentService;
import com.mentalhealth.service.CounselorService;
import com.mentalhealth.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private CounselorService counselorService;

    @Autowired
    private JdbcTemplate jdbc;

    /** 获取启用的咨询师列表 */
    @GetMapping("/counselors")
    public ResultVO<List<Counselor>> counselors() {
        return ResultVO.success(counselorService.getEnabledCounselors());
    }

    /** 获取咨询师可预约时间段（根据日期计算星期几） */
    @GetMapping("/counselors/{id}/slots")
    public ResultVO<List<String>> counselorSlots(@PathVariable Long id, @RequestParam String date) {
        java.time.LocalDate localDate = java.time.LocalDate.parse(date);
        int weekDay = localDate.getDayOfWeek().getValue();
        List<String> slots = jdbc.queryForList(
                "SELECT time_slot FROM counselor_available_slot WHERE counselor_id = ? AND week_day = ? AND enabled = 1 ORDER BY time_slot",
                String.class, id, weekDay);
        return ResultVO.success(slots);
    }

    /** 创建预约 */
    @PostMapping("/create")
    public ResultVO<?> create(@RequestBody Appointment appointment, HttpServletRequest request) {
        if (appointment == null) {
            return ResultVO.badRequest("预约信息不能为空");
        }
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

    /** 获取某咨询师某日期已被预约的时间段 */
    @GetMapping("/booked-slots")
    public ResultVO<List<String>> bookedSlots(
            @RequestParam String counselorName,
            @RequestParam String date) {
        return ResultVO.success(appointmentService.getBookedSlots(counselorName, date));
    }

    /** 取消预约 */
    @PutMapping("/cancel/{id}")
    public ResultVO<?> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        appointmentService.cancelAppointment(id, userId);
        return ResultVO.success();
    }
}
