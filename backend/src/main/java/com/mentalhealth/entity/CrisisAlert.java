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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
