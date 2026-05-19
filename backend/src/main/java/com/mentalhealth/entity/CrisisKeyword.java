package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 危机关键词配置实体
 * 对应 crisis_keyword 表，替代前端/后端写死的关键词列表
 */
@Data
@TableName("crisis_keyword")
public class CrisisKeyword {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关键词 */
    private String keyword;

    /** 风险等级: high-高危, mid-中危, low-关注 */
    private String riskLevel;

    /** 是否启用: 1-启用, 0-禁用 */
    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
