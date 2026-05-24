package com.mentalhealth.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AssessmentCategory {
    private Long id;
    private String name;
    private Integer sortOrder;
    private Integer enabled;
    private LocalDateTime createdAt;
}
