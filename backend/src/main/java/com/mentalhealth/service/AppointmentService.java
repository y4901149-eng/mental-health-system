package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Appointment;
import java.util.List;

/**
 * 预约 Service 接口
 */
public interface AppointmentService extends IService<Appointment> {

    /** 创建预约 */
    void createAppointment(Appointment appointment);

    /** 获取用户的预约列表 */
    List<Appointment> getUserAppointments(Long userId);

    /** 取消预约 */
    void cancelAppointment(Long id, Long userId);

    /** 确认预约（管理员） */
    void confirmAppointment(Long id);

    /** 完成预约 */
    void completeAppointment(Long id);

    /** 获取某咨询师某日期已被预约的时间段 */
    List<String> getBookedSlots(String counselorName, String date);
}
