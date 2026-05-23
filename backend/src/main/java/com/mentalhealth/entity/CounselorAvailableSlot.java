package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("counselor_available_slot")
public class CounselorAvailableSlot {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long counselorId;

    private Integer weekDay;

    private String timeSlot;

    private Integer enabled;

    private LocalDateTime createdAt;
}
