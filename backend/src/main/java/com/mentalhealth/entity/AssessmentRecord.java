package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评估记录实体类
 * 作用：对应 assessment_record 表，记录用户每次做量表的结果
 */
@Data
@TableName("assessment_record")
public class AssessmentRecord {
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 量表ID */
    private Long assessmentId;

    /** 总分 */
    private Integer totalScore;

    /** 评估等级: normal-正常, mild-轻度, moderate-中度, severe-重度 */
    private String level;

    /** 评估建议 */
    private String suggestion;

    /** 提交时间 */
    private LocalDateTime submitTime;
}
