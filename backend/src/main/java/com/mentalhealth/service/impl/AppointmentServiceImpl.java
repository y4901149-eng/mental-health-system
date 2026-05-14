package com.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Appointment;
import com.mentalhealth.mapper.AppointmentMapper;
import com.mentalhealth.service.AppointmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预约 Service 实现类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Override
    public void createAppointment(Appointment appointment) {
        appointment.setStatus("pending"); // 初始状态：待确认
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
}
