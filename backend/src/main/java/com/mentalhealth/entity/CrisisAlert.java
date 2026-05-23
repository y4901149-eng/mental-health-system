package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("crisis_alert")
public class CrisisAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer alertLevel;
    private String handleStatus;
    private String emergencyContact;
    private String emergencyPhone;
    private String triggerReason;
    private Long handledBy;
    private String handleRemark;

    /** 关联用户名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String username;

    /** 是否已通知监护人 */
    private Integer guardianNotified;

    /** 通知时间 */
    private LocalDateTime guardianNotifyTime;

    /** 通知方式: 电话/微信/面谈 */
    private String guardianNotifyMethod;

    /** 通知备注 */
    private String guardianNotifyRemark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
