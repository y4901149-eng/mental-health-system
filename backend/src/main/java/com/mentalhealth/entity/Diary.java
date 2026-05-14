package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 日记实体类
 * 作用：对应 diary 表，存储用户的每日心情日记
 */
@Data
@TableName("diary")
public class Diary {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 日记标题 */
    private String title;

    /** 日记内容（支持纯文本） */
    private String content;

    /** 情绪标签（逗号分隔，如 "开心,平静"） */
    private String moodTags;

    /** 情绪评分 */
    private Double emotionScore;

    /** 情绪标签（备用） */
    private String emotionTags;

    /** 日记图片URL（逗号分隔） */
    private String images;

    /** 单张封面图 */
    private String imageUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
