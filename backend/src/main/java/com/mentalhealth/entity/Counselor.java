package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("counselor")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String specialty;

    private String intro;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
