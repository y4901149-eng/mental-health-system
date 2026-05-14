package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 情绪记录实体类
 * 作用：对应 mood_record 表，记录用户每日情绪状态，用于情绪趋势图表展示
 */
@Data
@TableName("mood_record")
public class MoodRecord {
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 情绪评分 1-10（1=非常差, 10=非常好） */
    private Integer moodScore;

    /** 情绪标签: happy-开心, sad-悲伤, anxious-焦虑, angry-愤怒, calm-平静, tired-疲惫 */
    private String moodTag;

    /** 今日备注 */
    private String note;

    /** 记录日期 */
    private String recordDate;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
