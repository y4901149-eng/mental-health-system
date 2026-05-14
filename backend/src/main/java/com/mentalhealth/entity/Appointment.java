package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约记录实体类
 * 作用：对应 appointment 表，存储用户预约心理咨询的记录
 */
@Data
@TableName("appointment")
public class Appointment {
    /** 预约ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 咨询师姓名 */
    private String counselorName;

    /** 预约日期 */
    private String appointmentDate;

    /** 预约时间段: 09:00-10:00, 10:00-11:00, ... */
    private String timeSlot;

    /** 咨询类型: individual-个体咨询, group-团体咨询, crisis-危机干预 */
    private String type;

    /** 备注信息 */
    private String remark;

    /** 状态: pending-待确认, confirmed-已确认, completed-已完成, cancelled-已取消 */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
