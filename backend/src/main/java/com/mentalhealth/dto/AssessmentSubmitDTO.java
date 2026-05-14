package com.mentalhealth.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 评估提交数据传输对象
 * 作用：接收用户提交的量表答案
 */
@Data
public class AssessmentSubmitDTO {
    /** 量表ID */
    @NotNull(message = "量表ID不能为空")
    private Long assessmentId;

    /** 答题结果: key=题目ID, value=选项分值 */
    private Map<Long, Integer> answers;
}
