package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 心理评估量表实体类
 * 作用：对应 assessment 表，定义一套评估问卷（如SDS抑郁量表、SAS焦虑量表）
 */
@Data
@TableName("assessment")
public class Assessment {
    /** 量表ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 量表标题 */
    private String title;

    /** 量表描述 */
    private String description;

    /** 量表类型: depression-抑郁, anxiety-焦虑, stress-压力, sleep-睡眠 */
    private String type;

    /** 封面图片 */
    private String coverImage;

    /** 状态: draft-草稿, published-发布, closed-关闭 */
    private String status;

    /** 所属板块ID, 关联 assessment_category.id */
    private Integer categoryId;

    /** 题目数量 */
    private Integer questionCount;

    /** 创建者ID */
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段：该量表下的所有题目 */
    @TableField(exist = false)
    private List<AssessmentQuestion> questions;
}
