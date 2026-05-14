package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 评估题目实体类
 * 作用：对应 assessment_question 表，存储量表中的每一道题目
 */
@Data
@TableName("assessment_question")
public class AssessmentQuestion {
    /** 题目ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属量表ID */
    private Long assessmentId;

    /** 题目序号 */
    private Integer sortOrder;

    /** 题目内容 */
    private String questionText;

    /** 选项A文本 */
    private String optionA;

    /** 选项B文本 */
    private String optionB;

    /** 选项C文本 */
    private String optionC;

    /** 选项D文本 */
    private String optionD;

    /** 选项A分值 */
    private Integer scoreA;

    /** 选项B分值 */
    private Integer scoreB;

    /** 选项C分值 */
    private Integer scoreC;

    /** 选项D分值 */
    private Integer scoreD;

    /** 题目类型: radio-单选, checkbox-多选 */
    private String questionType;
}
