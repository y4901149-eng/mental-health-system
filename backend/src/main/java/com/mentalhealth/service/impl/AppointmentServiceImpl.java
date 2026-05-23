package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Appointment;
import com.mentalhealth.mapper.AppointmentMapper;
import com.mentalhealth.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 预约 Service 实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private static final List<String> ACTIVE_STATUSES = Arrays.asList("pending", "confirmed");

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public void createAppointment(Appointment appointment) {
        validateAppointment(appointment);

        // 根据日期计算星期几
        int weekDay = java.time.LocalDate.parse(appointment.getAppointmentDate().trim()).getDayOfWeek().getValue();

        // 校验该老师在该日期对应星期是否开放该时间段
        Integer slotCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM counselor_available_slot s JOIN counselor c ON s.counselor_id = c.id " +
                "WHERE c.name = ? AND s.week_day = ? AND s.time_slot = ? AND s.enabled = 1",
                Integer.class, appointment.getCounselorName().trim(), weekDay, appointment.getTimeSlot().trim());
        if (slotCount == null || slotCount == 0) {
            throw new RuntimeException("该老师在该日期暂不支持所选时间段");
        }

        // 校验该时间段是否已被预约
        long duplicateCount = count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCounselorName, appointment.getCounselorName())
                .eq(Appointment::getAppointmentDate, appointment.getAppointmentDate())
                .eq(Appointment::getTimeSlot, appointment.getTimeSlot())
                .in(Appointment::getStatus, ACTIVE_STATUSES));
        if (duplicateCount > 0) {
            throw new RuntimeException("该咨询师在该时间段已被预约，请选择其他时间");
        }

        appointment.setCounselorName(appointment.getCounselorName().trim());
        appointment.setAppointmentDate(appointment.getAppointmentDate().trim());
        appointment.setTimeSlot(appointment.getTimeSlot().trim());
        appointment.setType(isBlank(appointment.getType()) ? "individual" : appointment.getType().trim());
        appointment.setRemark(isBlank(appointment.getRemark()) ? null : appointment.getRemark().trim());
        appointment.setStatus("confirmed");
        save(appointment);
    }

    @Override
    public List<Appointment> getUserAppointments(Long userId) {
        return list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getUserId, userId)
                .orderByDesc(Appointment::getCreateTime));
    }

    @Override
    public void cancelAppointment(Long id, Long userId) {
        Appointment appointment = getById(id);
        if (appointment == null) {
            throw new RuntimeException("预约记录不存在");
        }
        if (!appointment.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (!ACTIVE_STATUSES.contains(appointment.getStatus())) {
            throw new RuntimeException("当前状态不可取消");
        }
        appointment.setStatus("cancelled");
        updateById(appointment);
    }

    @Override
    public void confirmAppointment(Long id) {
        Appointment appointment = getById(id);
        if (appointment != null) {
            appointment.setStatus("confirmed");
            updateById(appointment);
        }
    }

    @Override
    public void completeAppointment(Long id) {
        Appointment appointment = getById(id);
        if (appointment != null) {
            appointment.setStatus("completed");
            updateById(appointment);
        }
    }

    private void validateAppointment(Appointment appointment) {
        if (appointment.getUserId() == null) {
            throw new RuntimeException("用户信息不能为空");
        }
        if (isBlank(appointment.getCounselorName())) {
            throw new RuntimeException("请选择咨询师");
        }
        if (isBlank(appointment.getAppointmentDate())) {
            throw new RuntimeException("请选择预约日期");
        }
        if (isBlank(appointment.getTimeSlot())) {
            throw new RuntimeException("请选择时间段");
        }
    }

    @Override
    public List<String> getBookedSlots(String counselorName, String date) {
        List<Appointment> list = list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCounselorName, counselorName)
                .eq(Appointment::getAppointmentDate, date)
                .in(Appointment::getStatus, ACTIVE_STATUSES));
        return list.stream().map(Appointment::getTimeSlot).collect(java.util.stream.Collectors.toList());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
